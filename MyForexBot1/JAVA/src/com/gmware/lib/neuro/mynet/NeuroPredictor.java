package com.gmware.lib.neuro.mynet;

import com.gmware.lib.games.holdem.common.DataLocation;
import com.gmware.lib.games.holdem.common.Holdem;
import com.gmware.lib.games.holdem.gameinfo.GameInfo;
import com.gmware.lib.neuro.net2.Predictor;
import com.gmware.lib.neuro.mynet.Maps.NeuroMapUnknown;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Загрузчик предсказателей из перечисления {@link NeuroMapType}
 */
public class NeuroPredictor implements Predictor {
    /**
     * Набор нейросетей, выдающих выходы actions-предсказателя.
     */
    private final NeuroMap[] actionsNets;
    /**
     * Набор нейросетей, выдающих выходы alphas-предсказателя.
     */
    private final NeuroMap[] alphasNets;
    /**
     * Описание предсказателя.
     */
    private final String desctiption;

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Загрузить набор нейросетей из одного файла.
     *
     * @param fileName   имя файла, в котором хранится набор нейросетей.
     * @param nets       массив, в который необходимо загрузить набор нейросетей.
     * @param logMarker  маркер, с которым выводить сообщения в лог.
     * @param logNetType описание типа нейросети для вывода сообщения в лог.
     * @throws IOException если произошла ошибка ввода-вывода.
     */
    private static void loadNets(final String fileName,
                                 final NeuroMap[] nets,
                                 final String logMarker,
                                 final String logNetType) throws IOException {
        Holdem.ac.info(logMarker + ": Loading " + logNetType + " nets from " + fileName);
        try (final DataInputStream dis = new DataInputStream(Holdem.ac.getInputStream(fileName, DataLocation.ENCRYPTED))) {
            for (int k = 0; k < nets.length; k++) {
                final NeuroMap map = NeuroMap.load(dis);
                if (map instanceof NeuroMapUnknown)
                    throw new RuntimeException("Can't load nets from " + fileName);
                nets[k] = map;
            }
        }
    }

    //- Блок реализации интерфейса Predictor ---------------------------------------------------------------------------

    @Override
    public double[] getActions(final GameInfo gi, final Predictor.PredictorContext predictorContext, final int situationCode, final double[] features) {
        return actionsNets[situationCode].propagateMulty(features);
    }

    @Override
    public Predictor.Stats getActionStats(final GameInfo gi, final Predictor.PredictorContext predictorContext, final int situationCode) {
        return actionsNets[situationCode].getNetStats();
    }

    @Override
    public double[] getAlphas(final GameInfo gi, final Predictor.PredictorContext predictorContext, final int situationCode, final double[] features) {
        return alphasNets[situationCode].propagateMulty(features);
    }

    @Override
    public Predictor.Stats getAlphaStats(final GameInfo gi, final Predictor.PredictorContext predictorContext, final int situationCode) {
        return alphasNets[situationCode].getNetStats();
    }

    @Override
    public String getDesctiption(final GameInfo gi, final Predictor.PredictorContext predictorContext) {
        return desctiption;
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Конструктор.
     *
     * @param dir                имя папки, где хранятся файлы нейросетей.
     * @param propertiesFileName имя файла с properties, содержаих описание файлов нейросетей
     * @param logMarker          маркер, с которым выводить сообщения
     * @param netsNum            количество нейросетей
     * @throws IOException если произошла ошибка ввода-вывода.
     */
    public NeuroPredictor(final String dir,
                          final String propertiesFileName,
                          final int netsNum,
                          final String logMarker,
                          final String desctiption) throws IOException {
        this.desctiption = desctiption;
        final String propertiesFullFileName = dir + propertiesFileName;
        final Properties properties = new Properties();
        properties.load(Holdem.ac.getInputStream(propertiesFullFileName, DataLocation.ENCRYPTED));
        final String actionsSHNetsFileName = dir + properties.getProperty("actions", null);
        Holdem.ac.info(logMarker + ": Loading [" + desctiption + "] nets");
        actionsNets = new NeuroMap[netsNum];
        loadNets(actionsSHNetsFileName, actionsNets, logMarker, "actions");
        final String alphasSHNetsFileName = dir + properties.getProperty("alphas", null);
        alphasNets = new NeuroMap[netsNum];
        loadNets(alphasSHNetsFileName, alphasNets, logMarker, "alphas");
    }
}
