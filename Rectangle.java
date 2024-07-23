import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.PI;

public class Rectangle implements Comparable<Rectangle> {
    double minAngle, maxAngle; //минимальный и максимальный угол, под которым виден прямоугольник из начала координат
    Point l, r; //левая верхняя и правая нижняя точки прямоугольника

    public Rectangle(Point a, Point b) {
        this.l = new Point(Math.min(a.x, b.x), Math.max(a.y, b.y));
        this.r = new Point(Math.max(a.x, b.x), Math.min(a.y, b.y));

        //нахождение углов (не рассматривается угол точки в начале координат)
        if (l.x != 0 || l.y != 0) {
            if (r.x != 0 || r.y != 0) {
                this.minAngle = Math.min(l.angle, r.angle);
                this.maxAngle = Math.max(l.angle, r.angle);
            }
            else {
                this.minAngle = l.angle;
                this.maxAngle = l.angle;
            }
        } else {
            this.minAngle = r.angle;
            this.maxAngle = r.angle;
        }

        var leftBottom = new Point(this.l.x, this.r.y);
        if (leftBottom.y != 0 || leftBottom.x != 0) {
            this.minAngle = Math.min(this.minAngle, leftBottom.angle);
            this.maxAngle = Math.max(this.maxAngle, leftBottom.angle);
        }

        var rightUp = new Point(this.r.x, this.l.y);
        if (rightUp.x != 0 || rightUp.y != 0) {
            var angle = rightUp.angle;
            if (angle == 0) {  //случай четвертой четверти
                if (l.quarter == 4 || r.quarter == 4 || leftBottom.quarter == 4) {
                    angle = 2 * PI;
                }
            }

            this.minAngle = Math.min(this.minAngle, angle);
            this.maxAngle = Math.max(this.maxAngle, angle);
        }

        if (this.maxAngle == 0) this.maxAngle = PI * 2;
    }

    @Override
    public int compareTo(Rectangle other) {
        if (this.minAngle == other.minAngle) {
            if (this.maxAngle > other.maxAngle) return 1;
            if (this.maxAngle < other.maxAngle) return -1;
        }
        if (this.minAngle > other.minAngle) return 1;
        if (this.minAngle < other.minAngle) return -1;
        if (this.l == other.l && this.r == other.r) return 0;
        return this.l.compareTo(other.l);
    }

    //разрезание прямоугольника по осям координат для удобства подсчётов
    public List<Rectangle> cut() {
        var rectangles = new ArrayList<Rectangle>();

        if (this.l.y * this.r.y < 0) {
            if (this.l.x * this.r.x < 0) {
                //на 4 части
                rectangles.add(new Rectangle(this.l, new Point(0, 0)));
                rectangles.add(new Rectangle(new Point(0, this.l.y), new Point(this.r.x, 0)));
                rectangles.add(new Rectangle(new Point(0, 0), this.r));
                rectangles.add(new Rectangle(new Point(this.l.x, 0), new Point(0, this.r.y)));
            } else {
                rectangles.add(new Rectangle(this.l, new Point(this.r.x, 0)));
                rectangles.add(new Rectangle(new Point(this.l.x, 0), this.r));
            }
        } else {
            //на 2 части
            if (this.l.x * this.r.x < 0) {
                rectangles.add(new Rectangle(new Point(0, this.l.y), this.r));
                rectangles.add(new Rectangle(this.l, new Point(0, this.r.y)));
            } else {
                //не разрезать
                rectangles.add(this);
            }
        }

        return rectangles;
    }

    @Override
    public String toString() {
        return "Rectangle{" +
                "l=" + l +
                ", r=" + r +
                '}';
    }
}