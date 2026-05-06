package com.example.idastar;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class HandleImage {
    private final int Size;
    private final int Length;
    protected int blank;
    protected int[] Value;
    private final Image img;
    private final double w;
    private final double h;
    private double sourceX;
    private double sourceY;
    private double sourceSize;
    private double cw;
    private double ch;
    private double width, height, cw1, ch1;
    private double align = 0;
    public boolean win = false;
    public HandleImage(Image img, int size, int [] val) {
        this.img = img;
        this.Size =size;
        Length = Size * Size;
        this.Value = val;
        if(img == null) {
            width = w = 400;
            height = h = 400;
        }
        else {
            width = w = img.getWidth();
            height = h = img.getHeight();
        }
        InitImage();
    }
    public void InitImage() {
        int kt = 400;
        if (img == null) {
            sourceX = 0;
            sourceY = 0;
            sourceSize = kt;
        } else {
            sourceSize = Math.min(w, h);
            sourceX = (w - sourceSize) / 2;
            sourceY = (h - sourceSize) / 2;
        }
        cw = sourceSize / Size;
        ch = sourceSize / Size;
        width = kt;
        height = kt;
        align = 0;
        cw1 = width / Size;
        ch1 = height / Size;
        blank = posBlank(Value);
    }
    public int posBlank(int[] Value) {//Tìm vị trí phần tử blank
        int pos=0;
        for(int i = 0; i < Length; i++)
            if(Value[i] == 0) {
                pos = i;
                break;
            }
        return pos;
    }
    public void paint(GraphicsContext g) {
        // Tạo khung số
        if(img == null) {
            drawFrame(g, Color.BLACK);
            for(int i = 0; i < Length; i++) {
                double x = (i % Size) * cw1;
                double y = (double) (i / Size) * ch1;
                if(Value[i] != 0) {
                    g.setFill(Color.WHITE);
                    g.fillRect(x + 0.5, y + 0.5, cw1 - 1, ch1 - 1);
                    drawTileBorder(g, x, y);
                } else {
                    g.setFill(Color.rgb(156, 127, 78));
                    g.fillRect(x + 0.5, y + 0.5, cw1 - 1, ch1 - 1);
                    drawTileBorder(g, x, y);
                }
            }
            drawOuterBorder(g);
        }
        // Tạo khung ảnh
        else {
            g.clearRect(0, 0, 400, 400);
            if (!win) {
                drawFrame(g, Color.rgb(32, 32, 32));
                double dx, dy, sx, sy;
                for (int i = 0; i < Length; i++) {
                    if (Value[i] != 0) {
                        int c = Value[i] + 1 - State.goal;
                        sx = sourceX + (c % Size) * cw;
                        sy = sourceY + (double) (c / Size) * ch;
                        dx = (i % Size) * cw1;
                        dy = (double) (i / Size) * ch1;
                        g.drawImage(img, sx, sy, cw, ch, dx + align + 0.5, dy + 0.5, cw1 - 1, ch1 - 1);
                        drawTileBorder(g, dx + align, dy);
                    }
                }
                drawOuterBorder(g);
            } else {
                g.clearRect(0, 0, 400, 400);
                g.drawImage(img, sourceX, sourceY, sourceSize, sourceSize, 0, 0, width, height);
                drawOuterBorder(g);
            }
        }
    }

    private void drawFrame(GraphicsContext g, Paint color) {
        g.setFill(color);
        g.fillRect(0, 0, width, height);
    }

    private void drawTileBorder(GraphicsContext g, double x, double y) {
        g.setStroke(Color.rgb(245, 245, 245, 0.85));
        g.setLineWidth(1.2);
        g.strokeRect(x + 0.5, y + 0.5, cw1 - 1, ch1 - 1);
    }

    private void drawOuterBorder(GraphicsContext g) {
        g.setStroke(Color.rgb(28, 35, 45));
        g.setLineWidth(4);
        g.strokeRect(2, 2, width - 4, height - 4);
    }
}
