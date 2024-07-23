import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;

//хранение вводимой информации
public class Input {
    private final ArrayList<Rectangle> rectangles = new ArrayList<>();
    private final ArrayList<Point> points = new ArrayList<>();

    public void add(Rectangle rectangle) {
        rectangles.add(rectangle);
    }

    public void add(Point point) {
        points.add(point);
    }

    public ArrayList<Rectangle> rectangles() {
        return rectangles;
    }

    public List<Point> points() {
        return points;
    }
}
