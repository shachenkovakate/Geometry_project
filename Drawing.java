import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Drawing extends JPanel implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {
    private boolean shiftIsPressed = false;
    private Input input = new Input();  //данные, введенные пользователем. Изначально пустые
    private Solution.Answer answer = null;  //текущий ответ или null, если его пока нет
    private boolean drawingMax = false;
    private boolean drawingMin = false;

    // Настройки
    private int inter = 20; //интервал в пикселях на единичный отрезок оси
    private double eraserRad = 2; //радиус ластика
    private Color backgroundColor = Color.white;
    private Color coordColor = Color.black;
    private Color pointsColor = Color.black;
    private Color rectanglesColor = Color.black;
    private Color lineColor = Color.black;
    private Color highlightColor = Color.red;

    private int x1, y1, x2, y2, xMouse, yMouse; //координаты мыши при перемещении её с зажатой кнопкой
    private boolean dragged; //зажата ли кнопка мыши

    private boolean drawNet = false;

    private State state = State.IDLE;

    private Runnable onMouseMoved = () -> {};

    //состояние ввода
    private enum State {
        INPUT_RECTANGLES,
        INPUT_POINTS,
        IDLE,//мышью не рисуется ничего
        ERASING
    }

    Drawing(){
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        addMouseWheelListener(this);
    }

    public void paint(Graphics g) {
        //достаем размеры окна
        var height = getHeight();
        var width = getWidth();

        //достаем данные из текущего ввода
        var listOfPoints = input.points();
        var listOfRects = input.rectangles();

        //фон выбранного цвета
        g.setColor(backgroundColor);
        g.fillRect(0, 0, width, height);

        //оси координат противоположного фону цвета
        g.setColor(coordColor);

        Graphics2D gr = (Graphics2D) g; //создаем "кисть" для рисования

        if (drawNet) {
            gr.setStroke(new BasicStroke(2)); //толщина линии 2
        }

        g.drawLine(width / 2, height, width / 2, 0);
        g.drawLine(0, height / 2, width, height / 2);
        g.setFont(new Font("Cambria", Font.PLAIN, 20));
        g.drawString("X", width - 15, height / 2 + 18);
        g.drawString("Y", width / 2 - 22, 20);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 10));

        //деления осей
        for (int pic = width / 2 % inter + inter, cur = - (width / 2 / inter - 1); pic < width - 20; cur++, pic += inter) {
            if (cur == 0) continue;

            if (drawNet) {
                gr.setStroke(new BasicStroke(1));
                g.setColor(Color.gray);
                g.drawLine(pic, height, pic, 0);
                gr.setStroke(new BasicStroke(2));
                g.setColor(coordColor);
            }

            g.drawLine(pic, height / 2 + 4, pic, height / 2 - 4);
            if (cur > 0) g.drawString(Integer.toString(cur), pic - 3, height / 2 + 15);
            if (cur < 0) g.drawString(Integer.toString(cur), pic - 6, height / 2 + 15);
        }
        for (int pic = height / 2 % inter + inter, cur = (height / 2 / inter - 1); pic < height - 20; cur--, pic += inter) {
            if (cur == 0) continue;

            if (drawNet) {
                gr.setStroke(new BasicStroke(1));
                g.setColor(Color.gray);
                g.drawLine(0, pic, width, pic);
                gr.setStroke(new BasicStroke(2));
                g.setColor(coordColor);
            }

            g.drawLine(width / 2 - 4, pic, width / 2 + 4, pic);
            if (cur > 0) {
                if (cur / 10 > 0) g.drawString(Integer.toString(cur), width / 2 - 20, pic + 3);
                if (cur / 10 == 0) g.drawString(Integer.toString(cur), width / 2 - 15, pic + 3);
            }
            if (cur < 0) {
                if ((-cur) / 10 > 0) g.drawString(Integer.toString(cur), width / 2 - 24, pic + 3);
                if ((-cur) / 10 == 0) g.drawString(Integer.toString(cur), width / 2 - 18, pic + 3);
            }
        }

        //рисование точек выбранного цвета
        g.setColor(pointsColor);
        for (Point pt : listOfPoints) {
            g.fillOval(width / 2 + (int)(pt.dist * Math.cos(pt.angle) * inter) - 2,
                    height / 2 - (int)(pt.dist * Math.sin(pt.angle) * inter) - 2, 4, 4);
        }

        //рисование прямоугольников выбранного цвета
        g.setColor(rectanglesColor);
        for (var rect : listOfRects) {
            g.drawRect(width / 2 + (int)(rect.l.dist * Math.cos(rect.l.angle) * inter),
                    height / 2 - (int)(rect.l.dist * Math.sin(rect.l.angle) * inter),
                    (int)((rect.r.x - rect.l.x) * inter), (int)((rect.l.y - rect.r.y) * inter));
        }
        //рисование прямоугольника при перемещении мыши с зажатой кнопкой
        if (dragged) g.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
        if (state == State.ERASING) g.drawOval(xMouse - (int)(eraserRad * inter), yMouse - (int)(eraserRad * inter), (int)(eraserRad * inter * 2), (int)(eraserRad * inter * 2));

        if (drawingMax) drawAnswerMax(g, width, height);
        if (drawingMin) drawAnswerMin(g, width, height);
    }

    public void drawAnswerMax(Graphics g, int width, int height) {
        //рисование прямой максимального ответа выбранного цвета самой прямой и выделенных частей
        if (answer != null && answer.resultPointMax() != null) {
            var point = answer.resultPointMax();
            g.setColor(lineColor);
            g.drawLine(width / 2, height / 2,
                    width / 2 + (int)(point.x * inter),
                    height / 2 - (int)(point.y * inter));

            g.setColor(highlightColor);
            for (var segment: answer.segmentsMax()) {
                g.drawLine(
                        width / 2 + (int)(segment.start() * Math.cos(point.angle) * inter),
                        height / 2 - (int)(segment.start() * Math.sin(point.angle) * inter),
                        width / 2 + (int)(segment.end() * Math.cos(point.angle) * inter),
                        height / 2 - (int)(segment.end() * Math.sin(point.angle) * inter));
            }
        }
    }

    public void drawAnswerMin(Graphics g, int width, int height) {
        //рисование прямой минимального ответа выбранного цвета самой прямой и выделенных частей
        if (answer != null && answer.resultPointMin() != null) {
            var point = answer.resultPointMin();
            g.setColor(lineColor);
            g.drawLine(width / 2, height / 2,
                    width / 2 + (int) (point.x * inter),
                    height / 2 - (int) (point.y * inter));

            g.setColor(highlightColor);
            for (var segment : answer.segmentsMin()) {
                g.drawLine(
                        width / 2 + (int) (segment.start() * Math.cos(point.angle) * inter),
                        height / 2 - (int) (segment.start() * Math.sin(point.angle) * inter),
                        width / 2 + (int) (segment.end() * Math.cos(point.angle) * inter),
                        height / 2 - (int) (segment.end() * Math.sin(point.angle) * inter));
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //начало рисования прямоугольника мышью
        if (state == State.INPUT_RECTANGLES) {
            //координаты неподвижной вершины прямоугольника, другая вершина которого перемещается мышью
            x1 = e.getX();
            y1 = e.getY();
            answer = null;
        }

        if (state == State.INPUT_POINTS) {
            input.add(new Point(e.getX(), e.getY(), this.getHeight(), this.getWidth(), inter));
            answer = null;

            repaint();
        }

        erase(e);
    }

    private void erase(MouseEvent e) {
        if (state == State.ERASING) {
            var tempPoint = new Point(e.getX(), e.getY(), this.getHeight(), this.getWidth(), inter);
            var x = tempPoint.x;
            var y = tempPoint.y;
            for (int i = 0; i < input.points().size(); i++) {
                if (Math.abs(input.points().get(i).x - x) <= eraserRad && Math.abs(input.points().get(i).y - y) <= eraserRad) {
                    input.points().remove(i);
                    i--;
                }
            }

            for (int i = 0; i < input.rectangles().size(); i++) {
                if ((((Math.abs(input.rectangles().get(i).l.x - x) <= eraserRad ) || (Math.abs(input.rectangles().get(i).r.x - x) <= eraserRad))
                        && input.rectangles().get(i).l.y + eraserRad >= y && input.rectangles().get(i).r.y - eraserRad <= y) ||
                        (((Math.abs(input.rectangles().get(i).l.y - y) <= eraserRad) || (Math.abs(input.rectangles().get(i).r.y - y) <= eraserRad)) &&
                                input.rectangles().get(i).l.x - eraserRad <= x && input.rectangles().get(i).r.x + eraserRad >= x)) {
                    input.rectangles().remove(i);
                    i--;
                }
            }
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //окончание рисования прямоугольника мышью
        if (state == State.INPUT_RECTANGLES && dragged) {
            input.add(new Rectangle(new Point(Math.min(x1, x2), Math.min(y1, y2), this.getHeight(), this.getWidth(), inter),
                    new Point(Math.max(x1, x2), Math.max(y1, y2), this.getHeight(), this.getWidth(), inter)));
        }
        dragged = false; //далее мышь не перемещается с зажатой кнопкой
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        dragged = false; //далее мышь не перемещается с зажатой кнопкой
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (state == State.INPUT_RECTANGLES) {
            //координаты подвижной вершины прямоугольника, которая перемещается мышью
            dragged = true; //мышь всё ещё перемещается с зажатой кнопкой
            answer = null;
            x2 = e.getX();
            y2 = e.getY();
            repaint();
        }

        xMouse = e.getX();
        yMouse = e.getY();
        erase(e);

        onMouseMoved.run();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        xMouse = e.getX();
        yMouse = e.getY();
        onMouseMoved.run();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (shiftIsPressed) {
            System.out.println(e.getKeyCode());
        }
        System.out.println(e.getKeyCode());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.isShiftDown()) {
            shiftIsPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!e.isShiftDown()) shiftIsPressed = false;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (state == State.ERASING) {
            int a = e.getWheelRotation();
            eraserRad = Math.max(0.03, eraserRad - (double)a / 10);
            repaint();
        }
    }

    public void setAnswer(Solution.Answer answer) {
        this.answer = answer;
    }

    public void setInter(int inter) {
        this.inter = inter;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getCoordColor() { return coordColor; }

    public Color getPointsColor() {
        return pointsColor;
    }

    public Color getRectanglesColor() {
        return rectanglesColor;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setCoordColor(Color coordColor) {
        this.coordColor = coordColor;
    }

    public void setPointsColor(Color pointsColor) {
        this.pointsColor = pointsColor;
    }

    public void setRectanglesColor(Color rectanglesColor) {
        this.rectanglesColor = rectanglesColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }

    public void inputRectangles() {
        state = State.INPUT_RECTANGLES;
    }

    public void inputPoints() {
        state = State.INPUT_POINTS;
    }

    public void stopInput() {
        state = State.IDLE;
    }

    public void erasing() { state = State.ERASING; }

    public Input getInput() {
        return input;
    }

    public int getInter() {
        return inter;
    }

    public Point getMousePos() {
        return new Point(xMouse, yMouse, getHeight(), getWidth(), inter);
    }

    public void setOnMouseMoved(Runnable onMouseMoved) {
        this.onMouseMoved = onMouseMoved;
    }

    public void toggleDrawNet() {
        this.drawNet = !this.drawNet;
    }

    public void setDrawingMax(boolean a) { drawingMax = a; }
    public void setDrawingMin(boolean a) { drawingMin = a; }
}