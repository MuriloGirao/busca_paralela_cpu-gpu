import org.jocl.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.jocl.CL.*;

public class ParallelGPU {

    public static class Resultado {
        public int ocorrencias;
        public long tempoMillis;

        public Resultado(int ocorrencias, long tempoMillis) {
            this.ocorrencias = ocorrencias;
            this.tempoMillis = tempoMillis;
        }
    }

    public static Resultado contarPalavraGPU(String caminhoArquivo, String palavraBuscada) {
        long inicio = System.currentTimeMillis();

        // Carrega o texto
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                sb.append(linha).append(" ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String texto = sb.toString().toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "");
        String[] palavras = texto.split("\\s+");

        int n = palavras.length;
        byte[][] wordsBytes = new byte[n][];
        int maxLength = 0;
        for (int i = 0; i < n; i++) {
            wordsBytes[i] = palavras[i].getBytes(StandardCharsets.UTF_8);
            maxLength = Math.max(maxLength, wordsBytes[i].length);
        }

        // Flatten byte array
        byte[] flatWords = new byte[n * maxLength];
        for (int i = 0; i < n; i++) {
            System.arraycopy(wordsBytes[i], 0, flatWords, i * maxLength, wordsBytes[i].length);
        }

        byte[] palavraBuscadaBytes = palavraBuscada.toLowerCase().getBytes(StandardCharsets.UTF_8);

        // Inicialização do OpenCL
        CL.setExceptionsEnabled(true);
        cl_platform_id[] platforms = new cl_platform_id[1];
        clGetPlatformIDs(1, platforms, null);

        cl_device_id[] devices = new cl_device_id[1];
        clGetDeviceIDs(platforms[0], CL.CL_DEVICE_TYPE_GPU, 1, devices, null);

        cl_context context = clCreateContext(null, 1, devices, null, null, null);
        cl_command_queue queue = clCreateCommandQueue(context, devices[0], 0, null);

        String kernelSource =
                "__kernel void contar(__global const char* words, __global const char* target, "
                        + "int wordCount, int wordLen, int targetLen, __global int* result) {"
                        + "    int gid = get_global_id(0);"
                        + "    if (gid >= wordCount) return;"
                        + "    int match = 1;"
                        + "    for (int i = 0; i < targetLen; i++) {"
                        + "        if (words[gid * wordLen + i] != target[i]) {"
                        + "            match = 0;"
                        + "            break;"
                        + "        }"
                        + "    }"
                        + "    if (match) atomic_inc(result);"
                        + "}";

        cl_program program = clCreateProgramWithSource(context, 1, new String[]{kernelSource}, null, null);
        clBuildProgram(program, 0, null, null, null, null);
        cl_kernel kernel = clCreateKernel(program, "contar", null);

        // Buffers
        cl_mem wordsBuffer = clCreateBuffer(context, CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_char * flatWords.length, Pointer.to(flatWords), null);

        cl_mem targetBuffer = clCreateBuffer(context, CL.CL_MEM_READ_ONLY | CL.CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_char * palavraBuscadaBytes.length, Pointer.to(palavraBuscadaBytes), null);

        int[] resultado = new int[1];
        cl_mem resultBuffer = clCreateBuffer(context, CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_int, Pointer.to(resultado), null);

        // Argumentos
        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(wordsBuffer));
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(targetBuffer));
        clSetKernelArg(kernel, 2, Sizeof.cl_int, Pointer.to(new int[]{n}));
        clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{maxLength}));
        clSetKernelArg(kernel, 4, Sizeof.cl_int, Pointer.to(new int[]{palavraBuscadaBytes.length}));
        clSetKernelArg(kernel, 5, Sizeof.cl_mem, Pointer.to(resultBuffer));

        // Executa
        clEnqueueNDRangeKernel(queue, kernel, 1, null, new long[]{n}, null, 0, null, null);
        clEnqueueReadBuffer(queue, resultBuffer, CL.CL_TRUE, 0, Sizeof.cl_int, Pointer.to(resultado), 0, null, null);

        clReleaseMemObject(wordsBuffer);
        clReleaseMemObject(targetBuffer);
        clReleaseMemObject(resultBuffer);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(queue);
        clReleaseContext(context);

        long fim = System.currentTimeMillis();
        return new Resultado(resultado[0], fim - inicio);
    }

    public static void main(String[] args) {
        String arquivo = "C:\\Users\\muril\\OneDrive\\Documentos\\GitHub\\busca_paralela_cpu-gpu\\base_palavras.txt";
        String palavra = "gutenberg";

        Resultado resultado = contarPalavraGPU(arquivo, palavra);
        System.out.println("ParallelGPU: " + resultado.ocorrencias + " ocorrências em " + resultado.tempoMillis + " ms");
    }
}
