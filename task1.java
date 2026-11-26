import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class task1 extends JPanel {

    static final double V = 100;      
    static final int R = 80;       
    static final int N = 2;          

    static final int KILKIST = 3;    
    static final int ZATRYMKA = 20;   

    private volatile boolean pratsyuye = true;
    private final Object zamok = new Object();
    private final Random vypadkove = new Random();

    private final double[] rozrobnykX = new double[KILKIST];
    private final double[] rozrobnykY = new double[KILKIST];
    private final double[] rozrobnykKut = new double[KILKIST];

    private final double[] menedzherKut = new double[KILKIST];
    private final double[] menedzherX = new double[KILKIST];
    private final double[] menedzherY = new double[KILKIST];

    public task1() {
        setPreferredSize(new Dimension(600, 400));
        for (int i = 0; i < KILKIST; i++) {
            rozrobnykX[i] = 100 + vypadkove.nextInt(400);
            rozrobnykY[i] = 100 + vypadkove.nextInt(200);
            rozrobnykKut[i] = vypadkove.nextDouble() * 2 * Math.PI;
            menedzherKut[i] = i * 2 * Math.PI / KILKIST; // рівномірно по колу
        }
    }

    class PotikRozrobnykiv implements Runnable {
        public void run() {
            int lichylnyk = 0;
            double krok = V * ZATRYMKA / 1000.0;
            while (pratsyuye) {
                synchronized (zamok) {
                    lichylnyk++;
                    for (int i = 0; i < KILKIST; i++) {
                        if (lichylnyk * ZATRYMKA >= N * 1000) {
                            rozrobnykKut[i] = vypadkove.nextDouble() * 2 * Math.PI;
                        }
                        rozrobnykX[i] += krok * Math.cos(rozrobnykKut[i]);
                        rozrobnykY[i] += krok * Math.sin(rozrobnykKut[i]);

                        // біля краю вікна розвертаємось
                        if (rozrobnykX[i] < 0 || rozrobnykX[i] > getWidth()
                                || rozrobnykY[i] < 0 || rozrobnykY[i] > getHeight()) {
                            rozrobnykKut[i] += Math.PI;
                        }
                    }
                    if (lichylnyk * ZATRYMKA >= N * 1000) {
                        lichylnyk = 0;
                    }
                }
                repaint();
                try {
                    Thread.sleep(ZATRYMKA);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    class PotikMenedzheriv implements Runnable {
        public void run() {
            double zminaKuta = V / R * ZATRYMKA / 1000.0; // omega = V / R
            while (pratsyuye) {
                synchronized (zamok) {
                    int tsentrX = getWidth() / 2;
                    int tsentrY = getHeight() / 2;
                    for (int i = 0; i < KILKIST; i++) {
                        menedzherKut[i] += zminaKuta;
                        menedzherX[i] = tsentrX + R * Math.cos(menedzherKut[i]);
                        menedzherY[i] = tsentrY + R * Math.sin(menedzherKut[i]);
                    }
                }
                repaint();
                try {
                    Thread.sleep(ZATRYMKA);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        synchronized (zamok) {
            g.setColor(Color.BLUE);
            for (int i = 0; i < KILKIST; i++) {
                g.fillOval((int) rozrobnykX[i], (int) rozrobnykY[i], 20, 20);
            }
            g.setColor(Color.RED);
            for (int i = 0; i < KILKIST; i++) {
                g.fillOval((int) menedzherX[i] - 10, (int) menedzherY[i] - 10, 20, 20);
            }
        }
        g.setColor(Color.BLACK);
        g.drawString("Сині - розробники, червоні - менеджери", 10, 15);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame vikno = new JFrame("Лаб. №7");
            task1 panel = new task1();
            vikno.add(panel);
            vikno.pack();
            vikno.setLocationRelativeTo(null);
            vikno.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            vikno.setVisible(true);

            new Thread(panel.new PotikRozrobnykiv()).start();
            new Thread(panel.new PotikMenedzheriv()).start();
        });
    }
}