// Steps to launch the application:
// - download javafx-sdk-X.X.X
// - add the lib folder of javafx-sdk-X.X.X to File → Project Structure → Libraries
// - add --module-path "C:\Users\Viktor\Documents\workspace\TPO\lab9\javafx-sdk-21.0.7\lib" --add-modules javafx.controls,javafx.fxml
//   to Run → Edit Configurations → VM Options

package mgua;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import Matrix.Matrix;
import Matrix.MatrixMathematics;
import Matrix.NoSquareException;
import java.io.FileNotFoundException;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class MGUA extends Application {

    private static int nthreads;

    public static void main(String[] args) throws NoSquareException, IOException {
        launch("null");
    }

    @Override
    public void start(Stage stage) throws Exception {

        ArrayList<XYChart.Series> series = new ArrayList<>();

        Matrix[] matrices = getGMDHresult();
        series.add(getSeries( "Input XY", 0, 1, matrices));
        series.add(getSeries("Model XY",0, 2, matrices));

        createScene(stage, "Model", "X", "Y", series);

    }

    public Scene createScene(Stage stage, String title, String xLabel, String yLabel, ArrayList<XYChart.Series> series) {
        stage.setTitle(title);
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        yAxis.setLabel(yLabel);
        final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

        for (XYChart.Series s : series) {
            lineChart.getData().add(s);
        }
        Scene scene = new Scene(lineChart, 800, 600);
        stage.setScene(scene);
        stage.show();
        return scene;
    }

    public static Matrix[] getGMDHresult() throws NoSquareException, IOException, InterruptedException, ExecutionException {
        int[] numThreadsArr = new int[]{1, 2, 4, 6, 8, 10, 12, 14};
        int[] numPointsArr = new int[]{200, 300, 500, 1000, 1500};
        int[] numFeaturesArr = new int[]{5, 6, 7, 8, 9};

        double delta = 1;
        int seed = 10;

//        try (PrintWriter writer = new PrintWriter(new FileWriter("time.csv", false))) {     //TO WORK WITH GENERATED TASK
        try (PrintWriter writer = new PrintWriter(new FileWriter("energy.csv", false))) {               //TO WORK WITH PRACTICAL TASK
            writer.println("Threads,Points,Features,Time(ms)");

            Matrix[] result = null;
            for (int numThreads : numThreadsArr) {
                for (int numPoints : numPointsArr) {
                    for (int numFeatures : numFeaturesArr) {
                        nthreads = numThreads;
//                        double[][] data = getData(numPoints, numFeatures, delta, seed);                 //TO WORK WITH GENERATED TASK
                        double[][] data = readData("energy_consumption_dataset.tsv", 1000, 7);                     //TO WORK WITH PRACTICAL TASK

                        long t1 = System.nanoTime();

                        int n = data.length;
                        int m = data[0].length - 1;

                        int nA = n / 2 + n % 2;
                        int nB = n - nA;
                        double[][] xA = new double[nA][m];
                        double[] yA = new double[nA];
                        double[][] xB = new double[nB][m];
                        double[] yB = new double[nB];

                        for (int j = 0; j < m; j++) {
                            int ii = 0;
                            while (2 * ii < n) {
                                xA[ii][j] = data[2 * ii][j];
                                ii++;
                            }
                            ii = 0;
                            while (2 * ii + 1 < n) {
                                xB[ii][j] = data[2 * ii + 1][j];
                                ii++;
                            }
                        }
                        int ii = 0;
                        while (2 * ii < n) {
                            yA[ii] = data[2 * ii][m];
                            ii++;
                        }
                        ii = 0;
                        while (2 * ii + 1 < n) {
                            yB[ii] = data[2 * ii + 1][m];
                            ii++;
                        }

                        double[][] X = new double[nA][m + 1];
                        for (int j = 0; j < nA; j++) {
                            X[j][0] = 1;
                            System.arraycopy(xA[j], 0, X[j], 1, m);

                        }
                        double[][] Y = new double[nA][1];
                        for (int j = 0; j < nA; j++) {
                            Y[j][0] = yA[j];
                        }
                        double[][] XB = new double[nB][m + 1];
                        for (int j = 0; j < nB; j++) {
                            XB[j][0] = 1;
                            for (int i = 0; i < m; i++) {
                                XB[j][i + 1] = xB[j][i];
                            }
                        }
                        double[][] YB = new double[nB][1];
                        for (int j = 0; j < nB; j++) {
                            YB[j][0] = yB[j];
                        }

                        Matrix mX = new Matrix(X);
                        Matrix mY = new Matrix(Y);
                        Matrix mXB = new Matrix(XB);
                        Matrix mYB = new Matrix(YB);

                        Matrix mB = regressParam(mX, mY);
                        Matrix mYmod;
                        mYmod = MatrixMathematics.multiply(mX, mB, numThreads);

                        int[][] models = setOfModels(m);

                        PrintWriter output;
                        output = new PrintWriter(new FileWriter("RESULTS.txt"));

                        class ModelResult {
                            final int[] model;
                            final double crit;
                            final double reg;
                            final double unb;

                            ModelResult(int[] model, double crit, double reg, double unb) {
                                this.model = model;
                                this.crit = crit;
                                this.reg = reg;
                                this.unb = unb;
                            }
                        }

                        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
                        List<Callable<ModelResult>> tasks = new ArrayList<>();

                        for (int[] model : models) {
                            tasks.add(() -> {
                                double crit = getCriterion(model, mX, mY);
                                double reg = getCriterionReg(model, mX, mY, mXB, mYB);
                                double unb = getCriterionUnbiesedness(model, mX, mY, mXB, mYB);
                                return new ModelResult(model, crit, reg, unb);
                            });
                        }
                        List<Future<ModelResult>> results = executor.invokeAll(tasks);
                        executor.shutdown();


                        int[] modelOpt = models[0];
                        double min = results.getFirst().get().crit;
                        for (int j = 1; j < models.length; j++) {
                            if (results.get(j).get().crit < min) {
                                min = results.get(j).get().crit;
                                modelOpt = models[j];
                            }
                        }

                        double[][] XAB = new double[X.length + XB.length][m + 1];
                        for (int j = 0; j < X.length; j++) {
                            for (int i = 0; i <= m; i++) {
                                XAB[j][i] = X[j][i];
                            }
                        }
                        for (int j = X.length; j < X.length + XB.length; j++) {
                            for (int i = 0; i <= m; i++) {
                                XAB[j][i] = XB[j - X.length][i];
                            }
                        }

                        double[][] YAB = new double[X.length + XB.length][1];
                        for (int j = 0; j < X.length; j++) {
                            YAB[j][0] = yA[j];
                        }
                        for (int j = X.length; j < X.length + XB.length; j++) {
                            YAB[j][0] = yB[j - X.length];
                        }
                        long t2 = System.nanoTime();
                        System.out.printf("Results: %d threads, %d points, %d features, time(ms): %f \n",
                                numThreads, numPoints, numFeatures, (t2 - t1) * 1e-6);
                        writer.printf(Locale.US, "%d,%d,%d,%.1f\n", numThreads, numPoints, numFeatures, (t2 - t1) * 1e-6);

                        output.printf("\n Criterion = %5.9f", min);
                        System.out.print("\n Criterion = " + min);
                        if (min < 0.05) {
                            output.print("\n Congratulations! You have a quality model ");
                            System.out.print("\n Congratulations! You have a quality model ");
                        }
                        output.println();
                        System.out.println();
                        output.close();
                        mB = regressParam(subMatrix(modelOpt, new Matrix(XAB)), new Matrix(YAB));

                        mYmod = MatrixMathematics.multiply(subMatrix(modelOpt, new Matrix(XAB)), mB, numThreads);

                        result = new Matrix[3];
                        result[0] = new Matrix(XAB);
                        result[1] = new Matrix(YAB);
                        result[2] = mYmod;
                    }
                }
            }
            return result;
        }
    }

    public XYChart.Series getSeries(String name, int numResX, int numResY, Matrix[] matrices) throws NoSquareException, IOException, InterruptedException, ExecutionException {
        int n = matrices[0].getNrows();
        double[] x = new double[n];
        double[] y = new double[n];
      
        XYChart.Series seriesA = new XYChart.Series();
        seriesA.setName(name);
        //System.out.println("These points are drawn:");
        for (int j = 0; j < n; j++) {
            x[j] = matrices[numResX].getValues()[j][1];// Увага!!! Тут потрібна координата х...Потрібно вибирати!
            y[j] = matrices[numResY].getValues()[j][0];
        }

        for (int j = 0; j < x.length; j++) {

            seriesA.getData().add(new XYChart.Data(x[j], y[j]));

        }
        return seriesA;
    }
      

    private static void writeData(String name, double[][] data) {
        try {
            PrintWriter out;

            out = new PrintWriter(new FileWriter(name));

            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[0].length; j++) {
                    out.print(data[i][j]);
                    out.print(" ");
                    System.out.println("write " + data[i][j]);
                }
                out.println();
            }
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(MGUA.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static double[][] readData(String name, int rows, int cols) {
        double[][] data = new double[rows][cols];
        try {
            BufferedReader in;

            in = new BufferedReader(new FileReader(name));

            int i = 0;
            in.readLine(); // skip header
            String s = in.readLine();
            while (s != null) {
                StringTokenizer token = new StringTokenizer(s);
                int j = 0;
                while (token.hasMoreTokens()) {
                    data[i][j] = Double.parseDouble(token.nextToken());
                    j++;
                }
                i++;
                s = in.readLine();
            }
            in.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(MGUA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MGUA.class.getName()).log(Level.SEVERE, null, ex);
        }

        return data;
    }

    private static double[][] getData(int numPoints, int numFeatures, double delta, int seed) {
        int minXValue = -5;
        int maxXValue = 5;

        double[][] data = new double[numPoints][numFeatures + 1];
        Random rand = new Random(seed);

        for (int i = 0; i < numPoints; i++) {
            double y = -1.0;

            for (int j = 0; j < numFeatures; j++) {
                data[i][j] = rand.nextInt(maxXValue - minXValue + 1) + minXValue;
                y += -2.0 * data[i][j];
            }

            y += delta * rand.nextGaussian();
            data[i][numFeatures] = y;
        }

        return data;
    }

    private static double[] getCriterion(int[][] models, Matrix mx, Matrix my) throws NoSquareException {
        int n = models.length;
        double ySquares = getSquaresOfY(my);
        double[] c = new double[n];
        for (int j = 0; j < n; j++) {
            c[j] = getCriterion(models[j], mx, my) / ySquares;  //нормалізація критерію
        }

        return c;

    }

    private static double[] getCriterionReg(int[][] models, Matrix mx, Matrix my, Matrix mxB, Matrix myB) throws NoSquareException {
        int n = models.length;
        double ySquares = getSquaresOfY(myB);
        double[] c = new double[n];
        for (int j = 0; j < n; j++) {
            c[j] = getCriterionReg(models[j], mx, my, mxB, myB) / ySquares;//нормалізація критерію
        }

        return c;

    }

    private static double[] getCriterionUnbiesedness(int[][] models, Matrix mx, Matrix my, Matrix mxB, Matrix myB) throws NoSquareException {
        int n = models.length;
        double ySquares = getSquaresOfY(my) + getSquaresOfY(myB);
        double[] c = new double[n];
        for (int j = 0; j < n; j++) {
            c[j] = getCriterionUnbiesedness(models[j], mx, my, mxB, myB) / ySquares;//нормалізація критерію
            //System.out.println("CriterionUnbiesedness= " + c[j]);
        }

        return c;

    }

    private static double getSquaresOfY(Matrix my) throws NoSquareException {
        double c = 0;

        for (int j = 0; j < my.getNrows(); j++) {
            c += (my.getValueAt(j, 0)) * (my.getValueAt(j, 0));
        }
        return c;
    }

    private static double getCriterion(int[] model, Matrix mx, Matrix my) throws NoSquareException {
        double c = 0;
        Matrix XofModel = subMatrix(model, mx);
        Matrix mB = regressParam(XofModel, my);
        if (mB != null) {

            Matrix mYmod = MatrixMathematics.multiply(XofModel, mB, nthreads);
            for (int j = 0; j < my.getNrows(); j++) {
                c += (my.getValueAt(j, 0) - mYmod.getValueAt(j, 0)) * (my.getValueAt(j, 0) - mYmod.getValueAt(j, 0));
            }
        } else {
            return Double.MAX_VALUE;
        }
        return c;
    }

    private static double getCriterionReg(int[] model, Matrix mx, Matrix my, Matrix mxB, Matrix myB) throws NoSquareException {
        double c = 0;
        Matrix XofModel = subMatrix(model, mx);
        Matrix mB = regressParam(XofModel, my);
        if (mB != null) {
            Matrix mYmodOnB = MatrixMathematics.multiply(subMatrix(model, mxB), mB, nthreads);

            for (int j = 0; j < myB.getNrows(); j++) {
                c += (myB.getValueAt(j, 0) - mYmodOnB.getValueAt(j, 0)) * (myB.getValueAt(j, 0) - mYmodOnB.getValueAt(j, 0));
            }
        } else {
            return Double.MAX_VALUE;
        }
        return c;
    }

    private static double getCriterionUnbiesedness(int[] model, Matrix mx, Matrix my, Matrix mxB, Matrix myB) throws NoSquareException {
        double c = 0;
        //коефіцієнт екстраполяції =1
        Matrix XofModel = subMatrix(model, mx);
        Matrix mBforA = regressParam(XofModel, my);
        if (mBforA != null) {

            Matrix XBofModel = subMatrix(model, mxB);
            Matrix mBforB = regressParam(XBofModel, myB);
            if (mBforB != null) {

                Matrix mYmodOnAforA = MatrixMathematics.multiply(subMatrix(model, mx), mBforA, nthreads);
                Matrix mYmodOnAforB = MatrixMathematics.multiply(subMatrix(model, mxB), mBforA, nthreads);
                Matrix mYmodOnBforA = MatrixMathematics.multiply(subMatrix(model, mx), mBforB, nthreads);
                Matrix mYmodOnBforB = MatrixMathematics.multiply(subMatrix(model, mxB), mBforB, nthreads);

                for (int j = 0; j < my.getNrows(); j++) {
                    c += (mYmodOnAforA.getValueAt(j, 0) - mYmodOnBforA.getValueAt(j, 0)) * (mYmodOnAforA.getValueAt(j, 0) - mYmodOnBforA.getValueAt(j, 0));
                }

                for (int j = 0; j < myB.getNrows(); j++) {
                    c += (mYmodOnAforB.getValueAt(j, 0) - mYmodOnBforB.getValueAt(j, 0)) * (mYmodOnAforB.getValueAt(j, 0) - mYmodOnBforB.getValueAt(j, 0));
                }
            } else {
                return Double.MAX_VALUE;
            }
        } else {
            return Double.MAX_VALUE;
        }

        return c;
    }
    //Calculation of unbiesedness criterion with given extrapolation. In matrix mxC  - 
    // errors may be...
    private static double getCriterionUnbiesedness(int[] model, Matrix mx, Matrix my, Matrix mxB, Matrix myB,
            Matrix mxC) throws NoSquareException {
        double c = 0;
        //коефіцієнт екстраполяції =1
        Matrix XofModel = subMatrix(model, mx);
        Matrix mBforA = regressParam(XofModel, my);

        Matrix XBofModel = subMatrix(model, mxB);
        Matrix mBforB = regressParam(XBofModel, myB);
        if (mBforA == null || mBforB == null) {
            return Double.MAX_VALUE;
        }
        Matrix mYmodOnAforA = MatrixMathematics.multiply(subMatrix(model, mx), mBforA, nthreads);
        Matrix mYmodOnAforB = MatrixMathematics.multiply(subMatrix(model, mxB), mBforA, nthreads);
        Matrix mYmodOnBforA = MatrixMathematics.multiply(subMatrix(model, mx), mBforB, nthreads);
        Matrix mYmodOnBforB = MatrixMathematics.multiply(subMatrix(model, mxB), mBforB, nthreads);
        Matrix mYmodOnAforC = MatrixMathematics.multiply(subMatrix(model, mxC), mBforA, nthreads);
        Matrix mYmodOnBforC = MatrixMathematics.multiply(subMatrix(model, mxC), mBforB, nthreads);

        for (int j = 0; j < mYmodOnAforA.getNrows(); j++) {
            c += (mYmodOnAforA.getValueAt(j, 0) - mYmodOnBforA.getValueAt(j, 0)) * (mYmodOnAforA.getValueAt(j, 0) - mYmodOnBforA.getValueAt(j, 0));
        }

        for (int j = 0; j < mYmodOnAforB.getNrows(); j++) {
            c += (mYmodOnAforB.getValueAt(j, 0) - mYmodOnBforB.getValueAt(j, 0)) * (mYmodOnAforB.getValueAt(j, 0) - mYmodOnBforB.getValueAt(j, 0));
        }
        for (int j = 0; j < mYmodOnAforC.getNrows(); j++) {
            c += (mYmodOnAforC.getValueAt(j, 0) - mYmodOnBforC.getValueAt(j, 0)) * (mYmodOnAforC.getValueAt(j, 0) - mYmodOnBforC.getValueAt(j, 0));
        }
        double alfa = 1 + mxC.getNrows() / (my.getNrows() + myB.getNrows());
        c = c / (getSquaresOfY(my) * alfa); //нормалізація критерію
        return c;
    }

    private static Matrix regressParam(Matrix x, Matrix y) throws NoSquareException {
        Matrix mB;
        if (MatrixMathematics.determinant(MatrixMathematics.multiply(MatrixMathematics.transpose(x), x, nthreads)) == 0) {
            System.out.println("\n determinant==0 for such matrix: ");
            x.print();
            return null;
        }

        mB = MatrixMathematics.inverse(
                MatrixMathematics.multiply(MatrixMathematics.transpose(x), x, nthreads));
        mB = MatrixMathematics.multiply(mB,
                MatrixMathematics.multiply(MatrixMathematics.transpose(x), y, nthreads), nthreads);

        return mB;
    }

    private static void print(double[][] x) {
        for (double[] x1 : x) {
            for (double x2 : x1) {
                System.out.print(x2 + "\t");
            }
            System.out.println();
        }
    }

    private static double[][] dataX(int n, int m) {
        double[][] x = new double[n][m];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                x[j][i] = Math.ceil(-10 + 20 * Math.random());
            }
        }

        return x;
    }

    private static double[] dataY(double[][] dataX) {
        double[] y = new double[dataX.length];

        for (int j = 0; j < dataX.length; j++) {
            y[j] = 1.0;
            for (int i = 0; i < dataX[j].length; i++) {
                y[j] += 1.0 * dataX[j][i];
            }
            Random r = new Random();
            y[j] += r.nextGaussian();

        }

        return y;
    }

    private static double[] dataY(double[][] dataX, double[] b) {
        double[] y = new double[dataX.length];

        for (int j = 0; j < dataX.length; j++) {
            y[j] = b[0];
            for (int i = 0; i < dataX[j].length; i++) {
                y[j] += b[i + 1] * dataX[j][i];
            }
            Random r = new Random();
            y[j] += r.nextGaussian();

        }

        return y;
    }

    private static Matrix subMatrix(int[] model, Matrix X) {
        int cols = 0;
        for (int j : model) {
            if (j == 1) {
                cols++;
            }
        }
        Matrix subX = new Matrix(X.getNrows(), cols);
        int col = 0;
        for (int j = 0; j < model.length; j++) {
            if (model[j] == 1) {
                for (int i = 0; i < X.getNrows(); i++) {
                    subX.setValueAt(i, col, X.getValueAt(i, j));
                }
                col++;
            }

        }
 
        return subX;
    }

    private static int[][] setOfModels(int q) {

        int min = pow2(q) + 1;
        int max = 0;
        for (int i = 0; i <= q; i++) {
            max += pow2(i);
        }

        int[][] models = new int[pow2(q) - 1][q];
        int i = 0;
        for (int j = min; j <= max; j++) {
            models[i] = convertIntToBinary(q, j);
            i++;
        }

        return models;

    }

    private static int[] convertIntToBinary(int q, int r) {

        int[] w = new int[q + 1];
        int k = 0;
        for (int j = q; j >= 0; j--) {
            if (r < pow2(j)) {
                w[k] = 0;
                k++;
            } else {
                w[k] = r / pow2(j);
                r = r % pow2(j);
                k++;
            }
        }

        return w;
    }

    private static int pow2(int n) {
        int a = 1;
        if (n < 0) {
            return -1;
        }
        if (n == 0) {
            return 1;
        }
        for (int j = 1; j <= n; j++) {
            a *= 2;
        }
        return a;
    }

}
