import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

class PrintedEdition {
    private String name;
    private int year;
    private int pages;

    private double x;
    private double y;
    private double kut;

    public static int num_r = 0;

    public PrintedEdition(String n, int y, int p) {
        name = n;
        year = y;
        pages = p;
        num_r++;
    }

    public PrintedEdition(String in_name) {
        name = in_name;
        year = 2020;
        pages = 100;
        num_r++;
    }

    public PrintedEdition() {
        name = "Видання";
        year = 2000;
        pages = 50;
        num_r++;
    }

    public String getName() {
        return name;
    }

    public int getYear() {
        return year;
    }

    public int getPages() {
        return pages;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getKut() {
        return kut;
    }

    public void setX(double nx) {
        x = nx;
    }

    public void setY(double ny) {
        y = ny;
    }

    public void setKut(double nkut) {
        kut = nkut;
    }

    public void Show() {
        System.out.println(this.toString());
    }

    @Override
    public String toString() {
        String s;
        s = name + " " + year + " " + pages;
        return s;
    }

    @Override
    public boolean equals(Object obj) {
        boolean b = false;
        if (obj instanceof PrintedEdition) {
            PrintedEdition obj1 = (PrintedEdition) obj;
            if (name.equals(obj1.getName()) && year == obj1.getYear() && pages == obj1.getPages())
                b = true;
        }
        return b;
    }
}

class Magazine extends PrintedEdition {
    private int number;

    public Magazine(String n, int y, int p, int num) {
        super(n, y, p);
        number = num;
    }

    public Magazine(String in_name) {
        super(in_name);
        number = 1;
    }

    public Magazine() {
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "Magazine{} №" + number + " " + super.toString();
    }
}

class Book extends PrintedEdition {
    private String author;

    public Book(String n, int y, int p, String a) {
        super(n, y, p);
        author = a;
    }

    public Book(String in_name) {
        super(in_name);
        author = "Невідомий";
    }

    public Book() {
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return "Book{} " + author + " " + super.toString();
    }
}

class Textbook extends Book {
    private String subject;

    public Textbook(String n, int y, int p, String a, String subj) {
        super(n, y, p, a);
        subject = subj;
    }

    public Textbook(String in_name) {
        super(in_name);
        subject = "Загальний";
    }

    public Textbook() {
    }

    public String getSubject() {
        return subject;
    }

    @Override
    public String toString() {
        return "Textbook{} [" + subject + "] " + super.toString();
    }
}

abstract class BaseAI implements Runnable {
    protected final String imya;                      
    protected final Class<?> klasObyekta;             
    protected final List<PrintedEdition> kolektsiya;  
    protected final JPanel panel;                    
    protected final Random vypadkove = new Random();

    protected static final int ROZMIR = 20;         
    protected final double krok = 2;                 
    private final int zatrymka = 20;                 

    private volatile boolean pratsyuye = true;       
    private boolean pauza = false;                   

    public BaseAI(String imya, Class<?> klasObyekta, List<PrintedEdition> kolektsiya, JPanel panel) {
        this.imya = imya;
        this.klasObyekta = klasObyekta;
        this.kolektsiya = kolektsiya;
        this.panel = panel;
    }

    protected abstract void peremistyty(PrintedEdition e);

    // Відбиття від стінок вікна
    protected void vidbyttya(PrintedEdition e) {
        if (e.getX() < 0 || e.getX() > panel.getWidth() - ROZMIR) {
            e.setKut(Math.PI - e.getKut());
            e.setX(Math.max(0, Math.min(e.getX(), panel.getWidth() - ROZMIR)));
        }
        if (e.getY() < 0 || e.getY() > panel.getHeight() - ROZMIR) {
            e.setKut(-e.getKut());
            e.setY(Math.max(0, Math.min(e.getY(), panel.getHeight() - ROZMIR)));
        }
    }

    public synchronized void pryzupynyty() {
        pauza = true;
    }

    public synchronized void vidnovyty() {
        pauza = false;
        notifyAll();
    }

    public void zupynyty() {
        pratsyuye = false;
        vidnovyty();
    }

    private synchronized void chekajPauzu() throws InterruptedException {
        while (pauza) {
            wait();
        }
    }

    @Override
    public void run() {
        while (pratsyuye) {
            try {
                chekajPauzu();

                // доступ до спільної колекції
                synchronized (kolektsiya) {
                    for (PrintedEdition e : kolektsiya) {
                        if (e.getClass() == klasObyekta) {
                            peremistyty(e);
                        }
                    }
                }
                panel.repaint();

                Thread.sleep(zatrymka);
            } catch (InterruptedException ex) {
                break;
            }
        }
    }
}


// Друковане видання - хаотичний рух
class VydannyaAI extends BaseAI {
    public VydannyaAI(List<PrintedEdition> kolektsiya, JPanel panel) {
        super("Видання", PrintedEdition.class, kolektsiya, panel);
    }

    @Override
    protected void peremistyty(PrintedEdition e) {
        // напрямок щоразу трохи змінюється випадково
        e.setKut(e.getKut() + (vypadkove.nextDouble() - 0.5) * 0.4);
        e.setX(e.getX() + krok * Math.cos(e.getKut()));
        e.setY(e.getY() + krok * Math.sin(e.getKut()));
        vidbyttya(e);
    }
}

// Журнал - рух по колу
class ZhurnalAI extends BaseAI {
    private static final int R = 100; 

    public ZhurnalAI(List<PrintedEdition> kolektsiya, JPanel panel) {
        super("Журнал", Magazine.class, kolektsiya, panel);
    }

    @Override
    protected void peremistyty(PrintedEdition e) {
        e.setKut(e.getKut() + krok / R); // кутова швидкість = V / R
        e.setX(panel.getWidth() / 2 + R * Math.cos(e.getKut()));
        e.setY(panel.getHeight() / 2 + R * Math.sin(e.getKut()));
    }
}

// Книга - рух по горизонталі туди й назад
class KnyhaAI extends BaseAI {
    public KnyhaAI(List<PrintedEdition> kolektsiya, JPanel panel) {
        super("Книга", Book.class, kolektsiya, panel);
    }

    @Override
    protected void peremistyty(PrintedEdition e) {
        double napryamok = Math.signum(Math.cos(e.getKut())); // вправо (+1) або вліво (-1)
        e.setX(e.getX() + krok * napryamok);
        vidbyttya(e);
    }
}

// Підручник - рух по прямій з відбиттям від стінок
class PidruchnykAI extends BaseAI {
    public PidruchnykAI(List<PrintedEdition> kolektsiya, JPanel panel) {
        super("Підручник", Textbook.class, kolektsiya, panel);
    }

    @Override
    protected void peremistyty(PrintedEdition e) {
        e.setX(e.getX() + krok * Math.cos(e.getKut()));
        e.setY(e.getY() + krok * Math.sin(e.getKut()));
        vidbyttya(e);
    }
}

class PanelMalyuvannya extends JPanel {
    private final List<PrintedEdition> kolektsiya;

    public PanelMalyuvannya(List<PrintedEdition> kolektsiya) {
        this.kolektsiya = kolektsiya;
        setPreferredSize(new Dimension(700, 400));
        setBackground(Color.WHITE);
    }

    private Color kolir(PrintedEdition e) {
        if (e instanceof Textbook) return Color.GREEN;
        if (e instanceof Book) return Color.BLUE;
        if (e instanceof Magazine) return Color.RED;
        return Color.GRAY;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Очищення попереднього кадру
        synchronized (kolektsiya) {
            for (PrintedEdition e : kolektsiya) {
                g.setColor(kolir(e));
                g.fillOval((int) e.getX(), (int) e.getY(), BaseAI.ROZMIR, BaseAI.ROZMIR);
                g.setColor(Color.BLACK);
                g.drawString(e.getName(), (int) e.getX(), (int) e.getY() - 3);
            }
        }
        g.setColor(Color.BLACK);
        g.drawString("Сірі - видання, червоні - журнали, сині - книги, зелені - підручники", 10, 15);
    }
}


class Vikno extends JFrame {

    public Vikno(List<PrintedEdition> kolektsiya) {
        super("Лаб. №7. Завдання 2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        PanelMalyuvannya panel = new PanelMalyuvannya(kolektsiya);

        BaseAI[] potokyAI = {
                new VydannyaAI(kolektsiya, panel),
                new ZhurnalAI(kolektsiya, panel),
                new KnyhaAI(kolektsiya, panel),
                new PidruchnykAI(kolektsiya, panel)
        };
        Thread[] potoky = new Thread[potokyAI.length];


        JPanel panelKeruvannya = new JPanel(new GridLayout(potokyAI.length, 1));
        for (int i = 0; i < potokyAI.length; i++) {
            potoky[i] = new Thread(potokyAI[i]);
            panelKeruvannya.add(stvorytyPanelKeruvannya(potokyAI[i].imya, potokyAI[i], potoky[i]));
        }

        add(panel, BorderLayout.CENTER);
        add(panelKeruvannya, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        for (int i = 0; i < potoky.length; i++) {
            potoky[i].start();
        }
    }

    // Рядок керування одним видом
    private JPanel stvorytyPanelKeruvannya(String nazva, BaseAI ai, Thread potik) {
        JPanel p = new JPanel();
        p.add(new JLabel(nazva + ":"));

        JButton knopkaPauza = new JButton("Пауза");
        JButton knopkaProdovzhyty = new JButton("Продовжити");
        String[] priorytety = {"MIN_PRIORITY", "NORM_PRIORITY", "MAX_PRIORITY"};
        JComboBox<String> spysokPriorytetu = new JComboBox<>(priorytety);
        spysokPriorytetu.setSelectedIndex(1); // за замовчуванням NORM_PRIORITY

        knopkaPauza.addActionListener(e -> ai.pryzupynyty());
        knopkaProdovzhyty.addActionListener(e -> ai.vidnovyty());
        spysokPriorytetu.addActionListener(e -> {
            int nomer = spysokPriorytetu.getSelectedIndex();
            if (nomer == 0) {
                potik.setPriority(Thread.MIN_PRIORITY);
            } else if (nomer == 1) {
                potik.setPriority(Thread.NORM_PRIORITY);
            } else {
                potik.setPriority(Thread.MAX_PRIORITY);
            }
        });

        p.add(knopkaPauza);
        p.add(knopkaProdovzhyty);
        p.add(new JLabel("Пріоритет:"));
        p.add(spysokPriorytetu);
        return p;
    }
}

public class task2 {


    public static String zapytatyTekst(String pytannya) {
        String[] vidpovid = new String[1];
        try {
            SwingUtilities.invokeAndWait(() -> vidpovid[0] = JOptionPane.showInputDialog(pytannya));
        } catch (Exception e) {
            vidpovid[0] = null;
        }
        if (vidpovid[0] == null) {
            return "Без назви";
        }
        return vidpovid[0];
    }

    public static void main(String[] args) {

        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        } catch (Exception e) {
            System.out.println("Помилка налаштування кодування виводу");
        }


        Scanner in = new Scanner(System.in);


        System.out.print("Введіть кількість елементів масиву: ");
        int count = in.nextInt();
        in.nextLine();

        PrintedEdition[] arr = new PrintedEdition[count];

        for (int i = 0; i < count; i++) {
            System.out.println("\nОберіть тип (1 - Видання, 2 - Журнал, 3 - Книга, 4 - Підручник): ");
            int type = in.nextInt();
            in.nextLine();

            String name = zapytatyTekst("Введіть назву:");

            System.out.print("Введіть рік: ");
            int year = in.nextInt();

            System.out.print("Введіть кількість сторінок: ");
            int pages = in.nextInt();
            in.nextLine();

            if (type == 2) {
                System.out.print("Введіть номер журналу: ");
                int num = in.nextInt();
                in.nextLine();
                arr[i] = new Magazine(name, year, pages, num);
            } else if (type == 3) {
                String author = zapytatyTekst("Введіть автора:");
                arr[i] = new Book(name, year, pages, author);
            } else if (type == 4) {
                String author = zapytatyTekst("Введіть автора:");
                String subj = zapytatyTekst("Введіть предмет:");
                arr[i] = new Textbook(name, year, pages, author, subj);
            } else {
                arr[i] = new PrintedEdition(name, year, pages);
            }
        }

        System.out.println("\n--- Введений масив ---");
        for (int i = 0; i < arr.length; i++) {
            arr[i].Show();
        }

        in.close();
        System.out.println("Кількість створених об'єктів = " + PrintedEdition.num_r);

        // Спільна потокобезпечна колекція
        List<PrintedEdition> kolektsiya = Collections.synchronizedList(new ArrayList<>(Arrays.asList(arr)));

        Random vypadkove = new Random();
        for (PrintedEdition obyekt : kolektsiya) {
            obyekt.setX(20 + vypadkove.nextInt(640));
            obyekt.setY(20 + vypadkove.nextInt(340));
            obyekt.setKut(vypadkove.nextDouble() * 2 * Math.PI);
        }

        SwingUtilities.invokeLater(() -> new Vikno(kolektsiya));
    }
}