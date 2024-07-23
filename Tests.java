import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.sqrt;

public class Tests {
    record Case(
            List<Point> points,
            List<Rectangle> rectangles
    ) {}

    static Map<Case, Double> cases = new HashMap<>(Map.of(
            new Case(List.of(new Point(0, 0)), List.of(new Rectangle(new Point(-2, -2), new Point(2, 2)))), 0D,
            new Case(List.of(new Point(3, 3)), List.of(new Rectangle(new Point(-2, -2), new Point(2, 2)))), 2 * sqrt(2),
            new Case(List.of(new Point(0, 3)), List.of(new Rectangle(new Point(-2, -2), new Point(2, 2)))), 2D,
            new Case(List.of(new Point(-3, 3)), List.of(new Rectangle(new Point(-2, -2), new Point(2, 2)))), 2 * sqrt(2),
            new Case(List.of(new Point(-3, 0)), List.of(new Rectangle(new Point(-2, -2), new Point(2, 2)))), 2D,
            new Case(List.of(new Point(-3, -3)), List.of(new Rectangle(new Point(-2, -2), new Point(2, 2)))), 2 * sqrt(2),
            new Case(List.of(new Point(0, -3)), List.of(new Rectangle(new Point(-2, -2), new Point(2, 2)))), 2D,
            new Case(List.of(new Point(3, -3)), List.of(new Rectangle(new Point(-2, -2), new Point(2, 2)))), 2 * sqrt(2),
            new Case(List.of(new Point(3, 0)), List.of(new Rectangle(new Point(-2, -2), new Point(2, 2)))), 2D,
            new Case(List.of(new Point(3, 3)), List.of(new Rectangle(new Point(1, 1), new Point(2, 2)))), sqrt(2)
    ));
    static {
        cases.putAll(Map.of(
                new Case(List.of(new Point(-3, -3)), List.of(new Rectangle(new Point(-1, -1), new Point(-2, -2)))), sqrt(2),
                new Case(List.of(new Point(3, -3)), List.of(new Rectangle(new Point(1, -1), new Point(2, -2)))), sqrt(2),
                new Case(List.of(new Point(3, 3)), List.of(new Rectangle(new Point(0, 0), new Point(2, 2)),
                        new Rectangle(new Point(0, 0), new Point(1, 1)))), 2 * sqrt(2)
        ));
    }

    public static void main(String[] args) {
        for (var c: cases.entrySet()) {
            System.out.printf("Case %s, expected %s%n", c.getKey(), c.getValue());
            var input = new Input();

            c.getKey().points.forEach(input::add);
            c.getKey().rectangles.forEach(input::add);

            var ans = Solution.solve(input);
            if (Math.abs(c.getValue() - ans.lengthMax()) >= 0.0000001) {
                System.out.printf("Failed, expected %s, got %s%n", c.getValue(), ans.lengthMax());
            }
        }
    }

    public void test() {

    }
}
