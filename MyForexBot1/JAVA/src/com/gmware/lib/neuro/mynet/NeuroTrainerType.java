package com.gmware.lib.neuro.mynet;

import com.gmware.lib.games.holdem.common.Rnd;
import com.gmware.lib.neuro.NetImage;
import com.gmware.lib.neuro.mynet.Mappings.*;

import java.io.PrintStream;

/**
 * Тип тренера.
 * Created by Gauss on 11.12.2015.
 */
public enum NeuroTrainerType {

    CellsDefinition {
        @Override
        public NeuroMap train(final NetImage[] images,
                              final NeuroMapType mapType,
                              final Rnd rnd,
                              final PrintStream log,
                              final String netOutPath) {
            return NeuroMappingCellsDefinition.train(images, mapType, rnd, log, netOutPath);
        }

        @Override
        public NeuroMapping makeTrainer() {
            return new NeuroMappingCellsDefinition();
        }
    },
    PartitionByDetermination {
        @Override
        public NeuroMap train(final NetImage[] images,
                              final NeuroMapType mapType,
                              final Rnd rnd,
                              final PrintStream log,
                              final String netOutPath) {
            return NeuroMappingPartitionByDetermination.train(images, mapType, rnd, log, netOutPath);
        }

        @Override
        public NeuroMapping makeTrainer() {
            return new NeuroMappingPartitionByDetermination();
        }
    },
    PartitionByCorrelation4 {
        @Override
        public NeuroMap train(final NetImage[] images,
                              final NeuroMapType mapType,
                              final Rnd rnd,
                              final PrintStream log,
                              final String netOutPath) {
            return NeuroMappingPartitionByCorrelation4.train(images, mapType, rnd, log, netOutPath);
        }

        @Override
        public NeuroMapping makeTrainer() {
            return new NeuroMappingPartitionByCorrelation4();
        }
    },
    PartitionByCorrelation3 {
        @Override
        public NeuroMap train(final NetImage[] images,
                              final NeuroMapType mapType,
                              final Rnd rnd,
                              final PrintStream log,
                              final String netOutPath) {
            return NeuroMappingPartitionByCorrelation3.train(images, mapType, rnd, log, netOutPath);
        }

        @Override
        public NeuroMapping makeTrainer() {
            return new NeuroMappingPartitionByCorrelation3();
        }
    },
    PartitionByCorrelation2 {
        @Override
        public NeuroMap train(final NetImage[] images,
                              final NeuroMapType mapType,
                              final Rnd rnd,
                              final PrintStream log,
                              final String netOutPath) {
            return NeuroMappingPartitionByCorrelation2.train(images, mapType, rnd, log, netOutPath);
        }

        @Override
        public NeuroMapping makeTrainer() {
            return new NeuroMappingPartitionByCorrelation2();
        }
    },
    PartitionByCorrelation {
        @Override
        public NeuroMap train(final NetImage[] images,
                              final NeuroMapType mapType,
                              final Rnd rnd,
                              final PrintStream log,
                              final String netOutPath) {
            return NeuroMappingPartitionByCorrelation.train(images, mapType, rnd, log, netOutPath);
        }

        @Override
        public NeuroMapping makeTrainer() {
            return new NeuroMappingPartitionByCorrelation();
        }
    },
    LinearSegments {
        @Override
        public NeuroMap train(final NetImage[] images,
                              final NeuroMapType mapType,
                              final Rnd rnd,
                              final PrintStream log,
                              final String netOutPath) {
            return NeuroMappingLinearSegments.train(images, mapType, rnd, log, netOutPath);
        }

        @Override
        public NeuroMapping makeTrainer() {
            return new NeuroMappingLinearSegments();
        }
    },
    Spline {
        @Override
        public NeuroMap train(final NetImage[] images,
                              final NeuroMapType mapType,
                              final Rnd rnd,
                              final PrintStream log,
                              final String netOutPath) {
            return NeuroMappingSpline.train(images, mapType, rnd, log, netOutPath);
        }

        @Override
        public NeuroMapping makeTrainer() {
            return new NeuroMappingSpline();
        }
    },
    Polinom {
        @Override
        public NeuroMap train(final NetImage[] images,
                              final NeuroMapType mapType,
                              final Rnd rnd,
                              final PrintStream log,
                              final String netOutPath) {
            return NeuroMappingPolinom.train(images, mapType, rnd, log, netOutPath);
        }

        @Override
        public NeuroMapping makeTrainer() {
            return new NeuroMappingPolinom();
        }
    },
    Developing {
        @Override
        public NeuroNet train(final NetImage[] images,
                              final NeuroMapType netType,
                              final Rnd rnd,
                              final PrintStream log,
                              final String netOutPath) {
            return NeuroTrainerNormalRegressionFirst.train(images, netType, rnd, log, netOutPath);
        }

        @Override
        public NeuroMapping makeTrainer() {
            return new NeuroTrainerNormalRegressionFirst();
        }
    },
    StandartWithFixedInns {
        @Override
        public NeuroNet train(final NetImage[] images,
                              final NeuroMapType netType,
                              final Rnd rnd,
                              final PrintStream log,
                              final String netOutPath) {
            return NeuroTrainerStandartWithFixedInns.train(images, netType, rnd, log, netOutPath);
        }

        @Override
        public NeuroMapping makeTrainer() {
            return new NeuroTrainerStandartWithFixedInns();
        }
    },
    Standart {
        @Override
        public NeuroNet train(final NetImage[] images,
                              final NeuroMapType netType,
                              final Rnd rnd,
                              final PrintStream log,
                              final String netOutPath) {
            return NeuroTrainerStandart.train(images, netType, rnd, log, netOutPath);
        }

        @Override
        public NeuroMapping makeTrainer() {
            return new NeuroTrainerStandart();
        }
    };

    public abstract NeuroMap train(NetImage[] images, NeuroMapType netType, Rnd rnd, PrintStream log, String netOutPath);

    public abstract NeuroMapping makeTrainer();
}
