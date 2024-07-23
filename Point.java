public class Point implements Comparable<Point> {
    double dist, angle, x, y; //декартовы и полярные координаты точки
    int quarter; //четверть

    //ввод координат мышью и пересчёт их в координаты плоскости
    public Point(double x, double y, int canvasHeight, int canvasWidth, int coordInter) {
        this((x - (double) canvasWidth / 2) / coordInter, ((double) canvasHeight / 2 - y) / coordInter);
    }

    //создание точки через её координаты, вычисление остальных её параметров
    public Point(double x, double y) {
        this.x = x;
        this.y = y;

        if (this.x > 0 && this.y > 0) this.quarter = 1;
        if (this.x < 0 && this.y > 0) this.quarter = 2;
        if (this.x < 0 && this.y < 0) this.quarter = 3;
        if (this.x > 0 && this.y < 0) this.quarter = 4;
        this.dist = Math.sqrt(this.x * this.x + this.y * this.y);
        if (this.x == 0) {
            if (this.y > 0) this.angle = Math.PI / 2;
            else this.angle = Math.PI * 3 / 2;
        }
        else if (this.y == 0) {
            if (this.x > 0) this.angle = 0;
            else this.angle = Math.PI;
        }
        else this.angle = Math.atan(this.y / this.x);
        if (this.quarter == 2 || this.quarter == 3) this.angle += Math.PI;
        if (this.angle < 0) this.angle += Math.PI * 2;
        if (this.angle >= Math.PI * 2) this.angle -= Math.PI * 2;
    }


    @Override
    public int compareTo(Point point) {
        var comp = Double.compare(this.angle, point.angle);
        if (comp != 0) {
            return comp;
        }
        return Double.compare(this.dist, point.dist);
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}