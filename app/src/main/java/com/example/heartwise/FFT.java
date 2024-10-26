package com.example.heartwise; // Ensure this matches your package name

public class FFT {
    public static void fft(double[] real, double[] imag) {
        int n = real.length;

        if (n == 0) return;
        if ((n & (n - 1)) != 0) {
            throw new IllegalArgumentException("The number of samples is not a power of 2");
        }

        int logN = (int) (Math.log(n) / Math.log(2));

        // Bit-reversal permutation
        for (int i = 0; i < n; i++) {
            int j = Integer.reverse(i) >>> (32 - logN);
            if (j > i) {
                double tempReal = real[i];
                double tempImag = imag[i];
                real[i] = real[j];
                imag[i] = imag[j];
                real[j] = tempReal;
                imag[j] = tempImag;
            }
        }

        // FFT
        for (int s = 1; s <= logN; s++) {
            int m = 1 << s;
            int m2 = m >> 1;
            double theta = -2 * Math.PI / m;
            double wReal = 1.0;
            double wImag = 0.0;
            double wmReal = Math.cos(theta);
            double wmImag = Math.sin(theta);

            for (int j = 0; j < m2; j++) {
                for (int k = j; k < n; k += m) {
                    int t = k + m2;
                    double uReal = real[k];
                    double uImag = imag[k];
                    double tReal = real[t];
                    double tImag = imag[t];

                    double tempReal = wReal * tReal - wImag * tImag;
                    double tempImag = wReal * tImag + wImag * tReal;

                    real[k] = uReal + tempReal;
                    imag[k] = uImag + tempImag;
                    real[t] = uReal - tempReal;
                    imag[t] = uImag - tempImag;
                }
                double tempWReal = wReal * wmReal - wImag * wmImag;
                double tempWImag = wReal * wmImag + wImag * wmReal;
                wReal = tempWReal;
                wImag = tempWImag;
            }
        }
    }
}
