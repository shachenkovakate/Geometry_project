import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.Scanner;

public class Main extends JFrame {
    private final Drawing drawing = new Drawing();
    private final JLabel mousePositionLabel;
    private Instant nextRepaint = Instant.now();

    public Main(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        var menuBar = createMenu();
        setJMenuBar(menuBar);
        setBounds(0, 0, 1200, 800);
        setVisible(true);
        add(drawing);

        //ползунок изменения масштаба
        var scaleSlider = new JSlider(SwingConstants.HORIZONTAL);
        scaleSlider.setMaximum(500);
        scaleSlider.setMinimum(30);
        scaleSlider.setValue(drawing.getInter() * 5);
        scaleSlider.setMaximumSize(new Dimension(100, 20));
        scaleSlider.addChangeListener(e -> {
            var inter = scaleSlider.getValue();
            drawing.setInter(inter / 5);
            drawing.repaint();
        });

        //координаты курсора
        var statusBar = new JPanel();
        statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(statusBar, BorderLayout.SOUTH);

        this.mousePositionLabel = new JLabel();

        statusBar.add(mousePositionLabel, BorderLayout.WEST);
        statusBar.add(scaleSlider, BorderLayout.CENTER);

        //таймер обновления рисунка
        drawing.setOnMouseMoved(() -> {
            if (nextRepaint.toEpochMilli() <= Instant.now().toEpochMilli()) {
                repaint();
                nextRepaint = Instant.now().plus(Duration.ofMillis(30));
            }
        });
    }

    private JMenuBar createMenu() {
        JMenuBar menuBar = new JMenuBar(); //основное меню
        menuBar.add(createEnterMenu()); //меню ввода
        menuBar.add(createSettingsMenu()); //меню настроек
        menuBar.add(createEraseMenu());
        menuBar.add(createSaveMenu());
        menuBar.add(createBuildMenu());

        var info = new JMenuItem("Инфо"); //информация о выполняемой задаче
        KeyStroke ctrlIKeyStroke = KeyStroke.getKeyStroke("control I");
        info.setAccelerator(ctrlIKeyStroke);
        info.setMaximumSize(new Dimension(82, 50)); //задание размеров кнопки
        info.addActionListener(e -> JOptionPane.showMessageDialog(info, "<html><table width=400>"+
                "На плоскости задано множество точек и множество прямоугольников. Рассмотрим все " +
                "отрезки, которые начинаются в начале координат и заканчиваются в указанных " +
                "точках. Найти такую точку и такой отрезок, у которого наибольшая " +
                "суммарная длина (подмножество) лежит хоть в каком-нибудь из прямоугольников." +
                "В качестве ответа выделить точку, по которой построен найденный " +
                "отрезок и части отрезка, находящиеся в прямоугольниках. Также вывести " +
                "длину этих отрезков. \n" +
                "Доп. опция: найти точку с аналогичной минимальной суммой.\n\n" +
                "Об авторе: Шаченкова Екатерина, 10-1 класс ПФМЛ №239 \n" +
                "Учитель информатики: Черепанова Софья Валерьевна"));

        menuBar.add(info);
        return menuBar;
    }

    private JMenu createBuildMenu() {
        var build = new JMenu("Построить"); //нахождение ответа
        var buildMax = new JMenuItem("Максимум");
        var buildMin = new JMenuItem("Минимум");
        KeyStroke ctrlshiftXKeyStroke = KeyStroke.getKeyStroke("ctrl shift X");
        buildMax.setAccelerator(ctrlshiftXKeyStroke);
        //решение и его вывод
        buildMax.addActionListener(e -> {
            //решение задачи для максимума
            var answer = Solution.solve(drawing.getInput());
            drawing.setAnswer(answer);
            drawing.setDrawingMax(true);
            drawing.setDrawingMin(false);
            drawing.repaint();
            //вывод ответа, если он есть
            if (answer.resultPointMax() != null) {
                JOptionPane.showMessageDialog(null, "Ответ: " +
                        new DecimalFormat("#0.000").format(answer.lengthMax()), "Ответ", JOptionPane.INFORMATION_MESSAGE);
            } else {
                //вывод сообщения, что ответа нет
                JOptionPane.showMessageDialog(null, "Нет решения", "Ответ", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        build.add(buildMax);

        KeyStroke ctrlshiftNKeyStroke = KeyStroke.getKeyStroke("ctrl shift N");
        buildMin.setAccelerator(ctrlshiftNKeyStroke);
        buildMin.addActionListener(e -> {
            //решение задачи для минимума
            var answer = Solution.solve(drawing.getInput());
            drawing.setAnswer(answer);
            drawing.setDrawingMax(false);
            drawing.setDrawingMin(true);
            drawing.repaint();
            //вывод ответа, если он есть
            if (answer.resultPointMin() != null) {
                JOptionPane.showMessageDialog(null, "Ответ: " +
                        new DecimalFormat("#0.000").format(answer.lengthMin()), "Ответ", JOptionPane.INFORMATION_MESSAGE);
            } else {
                //вывод сообщения, что ответа нет
                JOptionPane.showMessageDialog(null, "Нет решения", "Ответ", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        build.add(buildMin);

        return build;
    }

    private JMenu createEraseMenu() {
        var erase = new JMenu("Стереть");
        var clearAll = new JMenuItem("Стереть всё");
        KeyStroke shiftAKeyStroke = KeyStroke.getKeyStroke("shift A");
        clearAll.setAccelerator(shiftAKeyStroke);
        clearAll.addActionListener(e -> {
            drawing.getInput().points().clear();
            drawing.getInput().rectangles().clear();
            repaint();
        });
        erase.add(clearAll);

        var clearAllPoints = new JMenuItem("Стереть все точки");
        KeyStroke shiftPKeyStroke = KeyStroke.getKeyStroke("shift P");
        clearAllPoints.setAccelerator(shiftPKeyStroke);
        clearAllPoints.addActionListener(e -> {
            drawing.getInput().points().clear();
            repaint();
        });
        erase.add(clearAllPoints);

        var clearAllRects = new JMenuItem("Стереть все прямоугольники");
        KeyStroke shiftRKeyStroke = KeyStroke.getKeyStroke("shift R");
        clearAllRects.setAccelerator(shiftRKeyStroke);
        clearAllRects.addActionListener(e -> {
            drawing.getInput().rectangles().clear();
            repaint();
        });
        erase.add(clearAllRects);

        var eraser = new JMenuItem("Ластик");
        KeyStroke ctrlEKeyStroke = KeyStroke.getKeyStroke("ctrl E");
        eraser.setAccelerator(ctrlEKeyStroke);
        eraser.addActionListener(e -> drawing.erasing());
        erase.add(eraser);

        return erase;
    }

    private JMenuItem createSaveMenu() {
        var save = new JMenu("Сохранить");
        save.setMaximumSize(new Dimension(85, 50));

        KeyStroke ctrlSKeyStroke = KeyStroke.getKeyStroke("ctrl S");
        var saveWithAnswer = new JMenuItem("С ответом");
        saveWithAnswer.setAccelerator(ctrlSKeyStroke);
        saveWithAnswer.addActionListener(e -> writeToFile(true));

        KeyStroke ctrlShiftSKeyStroke = KeyStroke.getKeyStroke("ctrl shift S");
        var saveWithoutAnswer = new JMenuItem("Без ответа");
        saveWithoutAnswer.setAccelerator(ctrlShiftSKeyStroke);
        saveWithoutAnswer.addActionListener(e -> writeToFile(false));

        save.add(saveWithAnswer);
        save.add(saveWithoutAnswer);

        return save;
    }

    //ввод
    private JMenu createEnterMenu() {
        JMenu enter = new JMenu("Ввод");

        //ввод с клавиатуры
        JMenuItem keyboard = new JMenuItem("Клавиатура");
        KeyStroke ctrlKKeyStroke = KeyStroke.getKeyStroke("ctrl K");
        keyboard.setAccelerator(ctrlKKeyStroke);
        keyboard.addActionListener(e -> {
            drawing.stopInput();

            JFrame frame = new JFrame();
            frame.setLayout(new FlowLayout());
            frame.setTitle("Ввод");
            frame.setSize(650, 200);

            //ввод точек
            JLabel jLabel1 = new JLabel("Точки: ");
            jLabel1.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jLabel1);
            JLabel jLabelx = new JLabel("x:");
            jLabelx.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jLabelx);
            JTextField jTextFieldx, jTextFieldy, jTextFieldx1, jTextFieldy1, jTextFieldx2, jTextFieldy2;
            jTextFieldx = new JTextField("",10);
            jTextFieldx.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jTextFieldx);
            JLabel jLabely = new JLabel("y:");
            jLabely.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jLabely);
            jTextFieldy = new JTextField("",10);
            jTextFieldy.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jTextFieldy);
            JButton jButton1 = new JButton("Ввести точку");
            jButton1.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jButton1);
            jButton1.addActionListener(k -> {
                double x = 0, y = 0;
                boolean er_a = false, er_b = false;

                //проверка правильности ввода данных
                try {
                    x = Double.parseDouble(jTextFieldx.getText());
                    er_a=true;
                } catch (Exception e1) {JOptionPane.showMessageDialog(null,"Error x", "Error", JOptionPane.ERROR_MESSAGE);}
                try {
                    y = Double.parseDouble(jTextFieldy.getText());
                    er_b=true;
                } catch (Exception e1) {JOptionPane.showMessageDialog(null,"Error y", "Error", JOptionPane.ERROR_MESSAGE);}

                //добавление точки при правильных введённых данных
                if (er_a && er_b) {
                    drawing.getInput().add(new Point(x, y));
                    jTextFieldx.setText("");
                    jTextFieldy.setText("");
                    repaint();
                }
            });

            //ввод прямоугольников
            JLabel jLabel2=new JLabel("Прямоугольники: ");
            jLabel2.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jLabel2);
            JLabel jLabelx1 = new JLabel(" x1:");
            jLabelx1.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jLabelx1);
            jTextFieldx1 = new JTextField("",10);
            jTextFieldx1.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jTextFieldx1);
            JLabel jLabely1 = new JLabel(" y1:");
            jLabely1.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jLabely1);
            jTextFieldy1 = new JTextField("",10);
            jTextFieldy1.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jTextFieldy1);
            JLabel jLabelx2 = new JLabel(" x2:");
            jLabelx2.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jLabelx2);
            jTextFieldx2 = new JTextField("",10);
            jTextFieldx2.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jTextFieldx2);
            JLabel jLabely2 = new JLabel(" y2:");
            jLabely2.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jLabely2);
            jTextFieldy2 = new JTextField("",10);
            jTextFieldy2.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jTextFieldy2);
            JButton jButton2 = new JButton("Ввести прямоугольник");
            jButton2.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jButton2);
            jButton2.addActionListener(k -> {
                double x1 = 0, y1 = 0, x2 = 0, y2 = 0;
                boolean er_x1 = false, er_y1 = false, er_x2 = false, er_y2 = false;

                //проверка правильности ввода данных
                try {
                    x1 = Double.parseDouble(jTextFieldx1.getText());
                    er_x1 = true;
                } catch (Exception e12) {JOptionPane.showMessageDialog(null,"Error x1", "Error", JOptionPane.ERROR_MESSAGE);}
                try {
                    y1 = Double.parseDouble(jTextFieldy1.getText());
                    er_y1 = true;
                } catch (Exception e12) {JOptionPane.showMessageDialog(null,"Error y1", "Error", JOptionPane.ERROR_MESSAGE);}
                try {
                    x2 = Double.parseDouble(jTextFieldx2.getText());
                    er_x2 = true;
                } catch (Exception e12) {JOptionPane.showMessageDialog(null,"Error x2", "Error", JOptionPane.ERROR_MESSAGE);}
                try {
                    y2 = Double.parseDouble(jTextFieldy2.getText());
                    er_y2 = true;
                } catch (Exception e12) {JOptionPane.showMessageDialog(null,"Error y2", "Error", JOptionPane.ERROR_MESSAGE);}

                //добавление прямоугольника при правильных введённых данных
                if (er_x1 && er_y1 && er_x2 && er_y2) {
                    drawing.getInput().add(new Rectangle(new Point(Math.min(x1, x2), Math.max(y1, y2)),
                            new Point(Math.max(x2, x1), Math.min(y2, y1))));
                    jTextFieldx1.setText("");
                    jTextFieldy1.setText("");
                    jTextFieldx2.setText("");
                    jTextFieldy2.setText("");
                    repaint();
                }
            });
            frame.setVisible(true);
        });


        //ввод мышью
        var mouse = new JMenu("Мышь");
        //ввод точек
        JMenuItem point = new JMenuItem("Точки");
        KeyStroke ctrlPKeyStroke = KeyStroke.getKeyStroke("ctrl P");
        point.setAccelerator(ctrlPKeyStroke);
        point.addActionListener(e -> drawing.inputPoints());
        //ввод прямоугольников
        JMenuItem rect = new JMenuItem("Прямоугольники");
        KeyStroke ctrlRKeyStroke = KeyStroke.getKeyStroke("ctrl R");
        rect.setAccelerator(ctrlRKeyStroke);
        rect.addActionListener(e -> drawing.inputRectangles());
        mouse.add(point);
        mouse.add(rect);

        //ввод из файла
        JMenuItem file = new JMenuItem("Файл");
        KeyStroke ctrlFKeyStroke = KeyStroke.getKeyStroke("ctrl F");
        file.setAccelerator(ctrlFKeyStroke);
        file.addActionListener(e -> {
            drawing.stopInput();
            readFromFile();
        });
        enter.add(keyboard);
        enter.add(mouse);
        enter.add(file);
        return enter;
    }

    //ввод из файла
    private void readFromFile() {
        var fileChooser = new JFileChooser();

        fileChooser.setDialogTitle("Выберите файл для ввода:");
        var res = fileChooser.showOpenDialog(this);

        if (res == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                try (var reader = new Scanner(new BufferedReader(new FileReader(file))).useLocale(Locale.US)) {
                    var pointNumber = reader.nextInt();
                    for (var i = 0; i < pointNumber; i++) {
                        var x = reader.nextDouble();
                        var y = reader.nextDouble();

                        drawing.getInput().add(new Point(x, y));
                    }

                    var rNumber = reader.nextInt();
                    for (var i = 0; i < rNumber; i++) {
                        var x1 = reader.nextDouble();
                        var y1 = reader.nextDouble();
                        var x2 = reader.nextDouble();
                        var y2 = reader.nextDouble();

                        drawing.getInput().add(new Rectangle(new Point(Math.min(x1, x2), Math.max(y1, y2)),
                                new Point(Math.max(x2, x1), Math.min(y2, y1))));
                    }
                }
                repaint();
            } catch (InputMismatchException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,"Файл не в правильном формате", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,"Не удалось прочитать файл");
            }
        }
    }

    private void writeToFile(boolean returnAnswer) {
        var fileChooser = new JFileChooser();

        fileChooser.setDialogTitle("Сохранить:");
        fileChooser.setApproveButtonText("Save");
        var res = fileChooser.showOpenDialog(this);

        if (res == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                try (var writer = new PrintWriter(new FileWriter(file))) {
                    var input = drawing.getInput();
                    writer.println(input.points().size());
                    for (var point: input.points()) {
                        writer.println("%s %s".formatted(point.x, point.y));
                    }

                    writer.println(input.rectangles().size());

                    for (var rectangle: input.rectangles()) {
                        writer.println("%s %s %s %s".formatted(rectangle.l.x, rectangle.l.y, rectangle.r.x, rectangle.r.y));
                    }

                    var ans = Solution.solve(input);

                    if ((ans.resultPointMax() != null || ans.resultPointMin() != null) && returnAnswer) {
                        writer.println("==================");
                        writer.println(ans.lengthMax());
                        if (ans.resultPointMax() != null) writer.println("Максимум: %s, %s \n".formatted(ans.resultPointMax().x, ans.resultPointMax().y));
                        writer.println(ans.lengthMin());
                        if (ans.resultPointMin() != null) writer.println("Минимум:  %s, %s".formatted(ans.resultPointMin().x, ans.resultPointMin().y));
                    }
                }
            } catch (InputMismatchException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,"Файл не в правильном формате", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,"Не удалось прочитать файл");
            }
        }
    }

    //настройки
    private JMenu createSettingsMenu() {
        JMenu settings = new JMenu("Настройки");

        //задание цветов

        //цвет точек
        var colPoint = new JMenuItem("Цвет точек");
        colPoint.addActionListener(t -> {
            drawing.setPointsColor(JColorChooser.showDialog(this, "Выберите цвет точек", drawing.getPointsColor()));
            repaint();
        });
        settings.add(colPoint);

        //цвет прямоугольников
        var colRect = new JMenuItem("Цвет прямоугольников");
        colRect.addActionListener(t -> {
            drawing.setRectanglesColor(JColorChooser.showDialog(this, "Выберите цвет прямоугольников", drawing.getRectanglesColor()));
            repaint();
        });
        settings.add(colRect);

        //цвет прямой
        var colLine = new JMenuItem("Цвет прямой");
        colLine.addActionListener(t -> {
            drawing.setLineColor(JColorChooser.showDialog(this, "Выберите цвет прямой", drawing.getLineColor()));
            repaint();
        });
        settings.add(colLine);

        //цвет выделенных частей прямой
        var colHigh = new JMenuItem("Цвет выделенных частей прямой");
        colHigh.addActionListener(t -> {
            drawing.setHighlightColor(JColorChooser.showDialog(this, "Выберите цвет выделенных частей прямой", drawing.getHighlightColor()));
            repaint();
        });
        settings.add(colHigh);

        //цвет фона
        var colBack = new JMenu("Цвет фона");
        var blackBack = new JMenuItem("Чёрный");
        blackBack.addActionListener(e -> {
            drawing.setBackgroundColor(Color.black);
            drawing.setCoordColor(Color.white);
            repaint();
        });
        colBack.add(blackBack);
        var whiteBack = new JMenuItem("Белый");
        whiteBack.addActionListener(e -> {
            drawing.setBackgroundColor(Color.white);
            drawing.setCoordColor(Color.black);
            repaint();
        });
        colBack.add(whiteBack);
        settings.add(colBack);

        //масштаб
        var scale = new JMenuItem("Масштаб");
        scale.addActionListener(e -> {
            JFrame frame = new JFrame();
            frame.setLayout(new FlowLayout());
            frame.setTitle("Масштаб");
            frame.setSize(550, 100);
            JLabel jLabel = new JLabel("Масштаб(%): ");
            jLabel.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jLabel);

            var jTextField = new JTextField("",10);
            jTextField.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jTextField);

            JButton jButton = new JButton("Ввести масштаб");
            jButton.setFont(new Font("Courier New", Font.PLAIN,22));
            frame.add(jButton);
            jButton.addActionListener(k -> {
                double x = 1;
                boolean er = false;

                //проверка правильности введённых данных
                try {
                    x = Double.parseDouble(jTextField.getText());
                    er = true;
                } catch (Exception e12) {JOptionPane.showMessageDialog(null,"Error");}
                if (er) {
                    if (x == 0) JOptionPane.showMessageDialog(null,"Error");
                    drawing.setInter((int)(20 * x / 100));
                }
                repaint();
            });
            frame.setVisible(true);
        });
        settings.add(scale);

        var enableNet = new JMenuItem();
        enableNet.setText("Включить/выключить сетку");
        enableNet.addActionListener(e -> {
            drawing.toggleDrawNet();
            repaint();
        });
        settings.add(enableNet);

        return settings;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        var currentPos = drawing.getMousePos();
        this.mousePositionLabel.setText("X: %.2f; Y: %.2f".formatted(currentPos.x, currentPos.y));
        this.mousePositionLabel.setIgnoreRepaint(false);
        this.mousePositionLabel.repaint();
    }

    public static void main(String[] args) {
        new Main().setVisible(true);
    }
}