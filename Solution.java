import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;

import static java.lang.Math.PI;

public class Solution {
    public record Answer(
            double lengthMax,
            double lengthMin,
            Point resultPointMax,
            Point resultPointMin,
            ArrayList<Segment> segmentsMax, //массив результирующих подотрезков, хранящийся как список расстояний до начала и конца каждого
            ArrayList<Segment> segmentsMin
    ) {}

    public record Segment( //отрезок. Хранится как расстояние от центра до начала и конца
        double start,
        double end
    ) {}

    //решает задачу с данным вводом. Возвращает ответ на задачу или null, если такого не существует
    public static Answer solve(Input input) {
        //копирование входных данных, чтобы модификации не меняли интерфейс
        var pointsList = new ArrayList<>(input.points());
        var rectSet = new TreeSet<Rectangle>();

        for (int i = 0; i < input.rectangles().size(); i++) {
            rectSet.addAll(input.rectangles().get(i).cut());
        }

        Collections.sort(pointsList);

        var ansPointMax = new Point(0, 1e9);
        double ansLengthMax = 0;
        var ansExistsMax = false;
        var ansPointMin = new Point(0, 1e9);
        double ansLengthMin = 1e9;
        var ansExistsMin = (!pointsList.isEmpty());
        ArrayList<Segment> ansSegmentsMax = new ArrayList<>();
        ArrayList<Segment> ansSegmentsMin = new ArrayList<>();

        //перебор точек
        for (var point : pointsList) {
            var list = new ArrayList<double[]>(); //массив нужных подотрезков (начало и конец, соответственно)
            var segments = new ArrayList<Segment>();

            double len = 0.0; //сумма длин нужных подотрезков

            //перебор прямоугольников
            for (int i = 0; i < rectSet.size(); i++) {
                int num = 0; //количество точек пересечения
                var rect = rectSet.stream().skip(i).findFirst().get(); //рассматриваемый прямоугольник

                if (point.dist == 0 && Math.min(rect.l.dist, Math.min(rect.r.dist,
                        Math.min(new Point(rect.l.x, rect.r.y).dist,
                                new Point(rect.r.x, rect.l.y).dist))) == 0) {
                    ansExistsMax = true;
                    continue;
                }

                if (rect.minAngle > point.angle && point.angle != 0) break; //этот и дальнейшие прямоугольники не будут подходить по углам
                if (equals(point.angle, 0) && rect.minAngle > point.angle && rect.maxAngle < PI * 2) continue;
                if (rect.maxAngle < point.angle) {
                    //этот прямоугольник не будет подходить для дальнейших точек
                    rectSet.remove(rect);
                    i--;
                    continue;
                }
                //точка ближе, чем самая близкая к началу координат точка прямоугольника
                if (point.dist < Math.min(rect.l.dist, Math.min(rect.r.dist,
                        Math.min(new Point(rect.l.x, rect.r.y).dist,
                                new Point(rect.r.x, rect.l.y).dist))))
                    continue;

                double x1 = 0, y1 = 0, x2 = 0, y2 = 0, x, y;

                //крайние случаи несуществующего тангенса угла или его равенства 0
                if (equals(point.angle, Math.PI / 2) || equals(point.angle, Math.PI * 3 / 2)) {
                    double d1;
                    double d2;
                    if (Math.abs(point.y) >= Math.max(Math.abs(rect.l.y), Math.abs(rect.r.y))) {
                        d1 = Math.abs(rect.l.y);
                        d2 = Math.abs(rect.r.y);

                    } else {
                        d1 = Math.min(Math.abs(rect.l.y), Math.abs(rect.r.y));
                        d2 = point.dist;

                    }
                    list.add(new double[]{Math.min(d1, d2), -1});
                    list.add(new double[]{Math.max(d1, d2), 1});
                    segments.add(new Segment(Math.min(d1, d2), Math.max(d1, d2)));
                    ansExistsMax = true;
                    continue;
                }
                if (equals(Math.tan(point.angle), 0)) {
                    double d1;
                    double d2;
                    if (Math.abs(point.x) >= Math.max(Math.abs(rect.l.x), Math.abs(rect.r.x))) {
                        d1 = Math.abs(rect.l.x);
                        d2 = Math.abs(rect.r.x);
                    } else {
                        d1 = Math.min(Math.abs(rect.l.x), Math.abs(rect.r.x));
                        d2 = point.dist;
                    }
                    list.add(new double[]{Math.min(d1, d2), -1});
                    list.add(new double[]{Math.max(d1, d2), 1});
                    segments.add(new Segment(Math.min(d1, d2), Math.max(d1, d2)));
                    ansExistsMax = true;
                    continue;
                }

                //пересечение с нижней стороной прямоугольника
                x = rect.r.y / Math.tan(point.angle);
                y = rect.r.y;
                if (x <= rect.r.x && x >= rect.l.x &&
                        Math.abs(point.y) >= Math.abs(y)) {
                    x1 = x;
                    y1 = y;
                    num++;
                }

                //пересечение с правой стороной прямоугольника
                x = rect.r.x;
                y = x * Math.tan(point.angle);
                if (y < rect.l.y && y > rect.r.y &&
                        Math.abs(point.x) >= Math.abs(x)) {
                    if (num == 0) {
                        x1 = x;
                        y1 = y;
                    } else {
                        x2 = x;
                        y2 = y;
                    }
                    num++;
                }

                //пересечение с верхней стороной прямоугольника
                x = rect.l.y / Math.tan(point.angle);
                y = rect.l.y;
                if (x <= rect.r.x && x >= rect.l.x &&
                        Math.abs(point.y) >= Math.abs(y)) {
                    if (num == 0) {
                        x1 = x;
                        y1 = y;
                    } else {
                        x2 = x;
                        y2 = y;
                    }
                    num++;
                }

                //пересечение с левой стороной прямоугольника
                x = rect.l.x;
                y = x * Math.tan(point.angle);
                if (y < rect.l.y && y > rect.r.y &&
                        Math.abs(point.x) >= Math.abs(x)) {
                    if (num == 0) {
                        x1 = x;
                        y1 = y;
                    } else {
                        x2 = x;
                        y2 = y;
                    }
                    num++;
                }

                //две точки пересечения
                if (num == 2) {
                    double d1 = new Point(x1, y1).dist;
                    double d2 = new Point(x2, y2).dist;
                    list.add(new double[]{Math.min(d1, d2), -1});
                    list.add(new double[]{Math.max(d1, d2), 1});
                    segments.add(new Segment(Math.min(d1, d2), Math.max(d1, d2)));
                }
                //рассматриваемая точка внутри прямоугольника
                if (num == 1) {
                    double d1 = new Point(x1, y1).dist;
                    double d2 = point.dist;
                    list.add(new double[]{Math.min(d1, d2), -1});
                    list.add(new double[]{Math.max(d1, d2), 1});
                    segments.add(new Segment(Math.min(d1, d2), Math.max(d1, d2)));
                }
                if (num > 0) ansExistsMax = true;
            }

            //нахождение объединения нужных подотрезков, чтобы лежащие в нескольких прямоугольниках не считались по несколько раз
            list.sort((double[] a, double[] b) -> {
                if (a[0] == b[0]) return Double.compare(a[1], b[1]);
                return Double.compare(a[0], b[0]);
            });
            int cur = 0; //внутри скольких подотрезков находится рассматриваемая точка
            boolean f = false; //находилась ли предыдущая точка внутри подотрезка
            double temp = 0; //начало подотрезка, конец которого мы ищем
            for (double[] doubles : list) {
                if (doubles[1] == -1 && cur == 0 && !f) {
                    //новый подотрезок только начинается, запоминаем его начало
                    temp = doubles[0];
                    f = true;
                }
                if (doubles[1] == -1) cur++; //начинается ещё один подотрезок, но мы уже внутри другого
                if (doubles[1] == 1 && cur == 1 && f) {
                    //выходим из всех подотрезков, прибавляем к искомой сумме длину последнего из объединения
                    len += doubles[0] - temp;
                    f = false;
                }
                if (doubles[1] == 1) cur--; //выходим из одного подотрезка, но остаёмся внутри другого
            }

            if (ansExistsMax && (len > ansLengthMax || (equals(ansLengthMax, len) && ansPointMax.dist > point.dist))) {
                //обновляем значение максимального ответа при необходимости
                ansPointMax = point;
                ansLengthMax = len;
                ansSegmentsMax = segments;
            }

            if (ansExistsMin && (len < ansLengthMin || (equals(ansLengthMin, len) && ansPointMin.dist > point.dist))) {
                //обновляем значение минимального ответа при необходимости
                ansPointMin = point;
                ansLengthMin = len;
                ansSegmentsMin = segments;
            }
        }

        //ни один отрезок из начала координат в точку не пересёк прямоугольники
        if (!ansExistsMax) {
            if (!ansExistsMin) return new Answer(ansLengthMax, ansLengthMin, null, null, ansSegmentsMax, ansSegmentsMin);
            return new Answer(ansLengthMax, ansLengthMin, null, ansPointMin, ansSegmentsMax, ansSegmentsMin);
        }

        return new Answer(ansLengthMax, ansLengthMin, ansPointMax, ansPointMin, ansSegmentsMax, ansSegmentsMin);
    }

    public static boolean equals(double a, double b) {
        return Math.abs(a - b) < 0.00000001;
    }
}