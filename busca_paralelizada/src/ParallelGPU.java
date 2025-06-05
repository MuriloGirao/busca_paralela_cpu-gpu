import org.jocl.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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

    public static Resultado contarPalavraComGPU(String caminhoArquivo, String palavraBuscada) {
        long inicioTempo = System.currentTimeMillis();

        // 1. Ler e preparar os dados
        StringBuilder textoCompleto = new StringBuilder();
        try (BufferedReader leitor = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                textoCompleto.append(linha).append(" ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String texto = textoCompleto.toString().toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "");
        String[] palavras = texto.split("\\s+");
        int numPalavras = palavras.length;
        int palavraLen = palavraBuscada.length();

        // 2. Preparar os dados para o kernel
        // Alinhar todas as palavras para o mesmo tamanho (pode truncar ou pad)
        int maxPalavraTamanho = palavraLen;
        byte[] flatPalavras = new byte[numPalavras * maxPalavraTamanho];
        for (int i = 0; i < numPalavras; i++) {
            String p = palavras[i];
            p = p.length() > palavraLen ? p.substring(0, palavraLen) : String.format("%-" + palavraLen + "s", p);
            byte[] bytes = p.getBytes();
            System.arraycopy(bytes, 0, flatPalavras, i * palavraLen, palavraLen);
        }

        byte[] palavraBuscadaBytes = palavraBuscada.toLowerCase().getBytes();

        // 3. Inicializar OpenCL
        CL.setExceptionsEnabled(true);
        int platformIndex = 0;
        int deviceIndex = 0;

        cl_platform_id[] platforms = new cl_platform_id[1];
        clGetPlatformIDs(1, platforms, null);
        cl_device_id[] devices = new cl_device_id[1];
        clGetDeviceIDs(platforms[platformIndex], CL_DEVICE_TYPE_GPU, 1, devices, null);

        cl_context context = clCreateContext(null, 1, devices, null, null, null);
        cl_command_queue queue = clCreateCommandQueue(context, devices[deviceIndex], 0, null);

        // 4. Kernel OpenCL
        String kernelSource =
            "__kernel void contar_palavra(__global const char* palavras, __global const char* alvo, __global int* saida, int palavraLen) {" +
            "   int id = get_global_id(0);" +
            "   int offset = id * palavraLen;" +
            "   int igual = 1;" +
            "   for (int i = 0; i < palavraLen; i++) {" +
            "       if (palavras[offset + i] != alvo[i]) {" +
            "           igual = 0;" +
            "           break;" +
            "       }" +
            "   }" +
            "   saida[id] = igual;" +
            "}";

        cl_program program = clCreateProgramWithSource(context, 1, new String[]{ kernelSource }, null, null);
        clBuildProgram(program, 0, null, null, null, null);
        cl_kernel kernel = clCreateKernel(program, "contar_palavra", null);

        // 5. Alocar buffers
        cl_mem palavrasMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_char * flatPalavras.length, Pointer.to(flatPalavras), null);
        cl_mem alvoMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_char * palavraLen, Pointer.to(palavraBuscadaBytes), null);
        cl_mem saidaMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY, Sizeof.cl_int * numPalavras, null, null);

        // 6. Setar argumentos
        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(palavrasMem));
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(alvoMem));
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(saidaMem));
        clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{ palavraLen }));

        // 7. Executar kernel
        long[] globalWorkSize = new long[]{ numPalavras };
        clEnqueueNDRangeKernel(queue, kernel, 1, null, globalWorkSize, null, 0, null, null);

        // 8. Ler resultado
        int[] resultadoArray = new int[numPalavras];
        clEnqueueReadBuffer(queue, saidaMem, CL_TRUE, 0, Sizeof.cl_int * numPalavras, Pointer.to(resultadoArray), 0, null, null);

        // 9. Somar as ocorrências
        int total = 0;
        for (int i : resultadoArray) {
            total += i;
        }

        // 10. Liberar recursos
        clReleaseMemObject(palavrasMem);
        clReleaseMemObject(alvoMem);
        clReleaseMemObject(saidaMem);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(queue);
        clReleaseContext(context);

        long fimTempo = System.currentTimeMillis();

        return new Resultado(total, fimTempo - inicioTempo);
    }
}
