import org.jocl.*;

public class ParallelGPU {	
    public static void main(String args[]) {
        CL.setExceptionsEnabled(true);

        int[] numPlatformsArray = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        cl_platform_id[] platforms = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);

        for (cl_platform_id platform : platforms) {
            byte[] buffer = new byte[1024];
            clGetPlatformInfo(platform, CL.CL_PLATFORM_NAME, buffer.length, Pointer.to(buffer), null);
            String platformName = new String(buffer, 0, findNull(buffer)).trim();
            System.out.println("Platform: " + platformName);

            int[] numDevicesArray = new int[1];
            clGetDeviceIDs(platform, CL.CL_DEVICE_TYPE_ALL, 0, null, numDevicesArray);
            int numDevices = numDevicesArray[0];
            System.out.println("Devices found: " + numDevices);
        }
    }

    private static int findNull(byte[] buffer) {
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] == 0) return i;
        }
        return buffer.length;
    }
}
