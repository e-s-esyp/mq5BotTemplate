package com.gmware.lib.neuro.mynet;

/**
 * ---
 * Created by Gauss on 13.06.2016.
 */
public class NeuroError {
    abstract public static class Error {
        NeuroComputationType type = new NeuroComputationType();

        abstract public double get(final NeuroBranch structure,
                            final NeuroImage[] images,
                            final int level);

        abstract public double get(final NeuroBranch structure,
                            final NeuroImage[] images,
                            final int level,
                            final int numImages);
    }

    public static Error[] errors = new Error[4];

    static {
        errors[0] = new Error() {
            @Override
            public double get(final NeuroBranch structure,
                       final NeuroImage[] images,
                       final int level) {
                return 0;
            }

            @Override
            public double get(final NeuroBranch structure,
                       final NeuroImage[] images,
                       final int level,
                       final int numImages) {
                return 0;
            }
        };
        errors[0].type.setFCR2();
        errors[1] = new Error() {
            @Override
            public double get(final NeuroBranch structure,
                       final NeuroImage[] images,
                       final int level) {
                return 0;
            }

            @Override
            public double get(final NeuroBranch structure,
                       final NeuroImage[] images,
                       final int level,
                       final int numImages) {
                return 0;
            }
        };
        errors[1].type.setFCR3();
        errors[2] = new Error() {
            @Override
            public double get(final NeuroBranch structure,
                       final NeuroImage[] images,
                       final int level) {
                return 0;
            }

            @Override
            public double get(final NeuroBranch structure,
                       final NeuroImage[] images,
                       final int level,
                       final int numImages) {
                return 0;
            }
        };
        errors[2].type.setAlphas();
        errors[3] = new Error() {
            @Override
            public double get(final NeuroBranch structure,
                       final NeuroImage[] images,
                       final int level) {
                double e = 0;
                for (final NeuroImage image : images) {
                    final NeuroBranch out = structure.propagateStructure(image.in, level);
                    e += image.out[out.out.klass];
                }
                e /= images.length;
                return -e;//минимизируем потери
            }

            @Override
            public double get(final NeuroBranch structure,
                       final NeuroImage[] images,
                       final int level,
                       final int numImages) {
                double e = 0;
                for (final NeuroImage image : images) {
                    final NeuroBranch out = structure.propagateStructure(image.in, level, numImages);
                    e += image.out[out.out.klass];
                }
                e /= images.length;
                return -e;//минимизируем потери
            }
        };
        errors[3].type.setF();
    }

}
