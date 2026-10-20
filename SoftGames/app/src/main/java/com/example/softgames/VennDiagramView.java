package com.example.softgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class VennDiagramView extends View {

    public static final int NONE = 0;
    public static final int A_INTERSECT_B = 1;           // A ∩ B
    public static final int A_UNION_B = 2;               // A ∪ B
    public static final int ONLY_A = 3;                  // A - B
    public static final int ONLY_B = 4;                  // B - A
    public static final int SYM_DIFF_AB = 5;             // A Δ B
    public static final int COMPLEMENT_UNION_AB = 6;     // (A ∪ B)'
    public static final int COMPLEMENT_A = 7;            // A'
    public static final int SET_A = 8;                   // Todo A
    public static final int A_INTERSECT_B_INTERSECT_C = 9; // A ∩ B ∩ C (3 conjuntos)
    public static final int A_UNION_B_UNION_C = 10;      // A ∪ B ∪ C (3 conjuntos)
    public static final int A_INTERSECT_B_MINUS_C = 11;  // (A ∩ B) - C (3 conjuntos)
    public static final int ONLY_C_3SET = 12;            // C - (A ∪ B) (3 conjuntos)

    private int diagramType = A_INTERSECT_B;

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint circleStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path pathUniverse = new Path();
    private final Path pathA = new Path();
    private final Path pathB = new Path();
    private final Path pathC = new Path();
    private final Path pathResult = new Path();
    private final Path pathTemp = new Path();
    private final RectF universeRect = new RectF();

    public VennDiagramView(Context context) {
        super(context);
        initPaints();
    }

    public VennDiagramView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    public VennDiagramView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaints();
    }

    private void initPaints() {
        float density = getResources().getDisplayMetrics().density;

        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(0xFFFAF5EC);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.5f * density);
        borderPaint.setColor(0xFF415A77);

        shadePaint.setStyle(Paint.Style.FILL);
        shadePaint.setColor(0xCC778D7A); // Verde salvia de la paleta oficial

        circleStrokePaint.setStyle(Paint.Style.STROKE);
        circleStrokePaint.setStrokeWidth(2.0f * density);
        circleStrokePaint.setColor(0xFF0D1B2A);

        textPaint.setColor(0xFF0D1B2A);
        textPaint.setTextSize(12f * density);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setDiagramType(int type) {
        this.diagramType = type;
        invalidate();
    }

    public int getDiagramType() {
        return diagramType;
    }

    private boolean isThreeSet() {
        return diagramType >= A_INTERSECT_B_INTERSECT_C;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float density = getResources().getDisplayMetrics().density;
        float pad = 4f * density;
        float cornerRadius = 10f * density;

        universeRect.set(pad, pad, w - pad, h - pad);
        pathUniverse.reset();
        pathUniverse.addRoundRect(universeRect, cornerRadius, cornerRadius, Path.Direction.CW);

        // Dibujar fondo del universo U
        canvas.drawPath(pathUniverse, bgPaint);

        pathA.reset();
        pathB.reset();
        pathC.reset();
        pathResult.reset();

        if (!isThreeSet()) {
            float cy = h * 0.54f;
            float r = Math.min(w * 0.27f, h * 0.34f);
            float offset = r * 0.58f;
            float cxA = (w * 0.5f) - offset;
            float cxB = (w * 0.5f) + offset;

            pathA.addCircle(cxA, cy, r, Path.Direction.CW);
            pathB.addCircle(cxB, cy, r, Path.Direction.CW);

            switch (diagramType) {
                case A_INTERSECT_B:
                    pathResult.op(pathA, pathB, Path.Op.INTERSECT);
                    break;
                case A_UNION_B:
                    pathResult.op(pathA, pathB, Path.Op.UNION);
                    break;
                case ONLY_A:
                    pathResult.op(pathA, pathB, Path.Op.DIFFERENCE);
                    break;
                case ONLY_B:
                    pathResult.op(pathB, pathA, Path.Op.DIFFERENCE);
                    break;
                case SYM_DIFF_AB:
                    pathResult.op(pathA, pathB, Path.Op.XOR);
                    break;
                case COMPLEMENT_UNION_AB:
                    pathTemp.op(pathA, pathB, Path.Op.UNION);
                    pathResult.op(pathUniverse, pathTemp, Path.Op.DIFFERENCE);
                    break;
                case COMPLEMENT_A:
                    pathResult.op(pathUniverse, pathA, Path.Op.DIFFERENCE);
                    break;
                case SET_A:
                    pathResult.set(pathA);
                    break;
                default:
                    break;
            }

            if (diagramType != NONE) {
                canvas.drawPath(pathResult, shadePaint);
            }

            canvas.drawPath(pathA, circleStrokePaint);
            canvas.drawPath(pathB, circleStrokePaint);

            textPaint.setTextSize(Math.max(10f * density, r * 0.42f));
            canvas.drawText("A", cxA - r * 0.45f, cy - r - (3f * density), textPaint);
            canvas.drawText("B", cxB + r * 0.45f, cy - r - (3f * density), textPaint);
        } else {
            float r = Math.min(w * 0.23f, h * 0.27f);
            float cxA = (w * 0.5f) - (r * 0.55f);
            float cyA = (h * 0.43f);
            float cxB = (w * 0.5f) + (r * 0.55f);
            float cyB = (h * 0.43f);
            float cxC = (w * 0.5f);
            float cyC = (h * 0.43f) + (r * 0.85f);

            pathA.addCircle(cxA, cyA, r, Path.Direction.CW);
            pathB.addCircle(cxB, cyB, r, Path.Direction.CW);
            pathC.addCircle(cxC, cyC, r, Path.Direction.CW);

            switch (diagramType) {
                case A_INTERSECT_B_INTERSECT_C:
                    pathTemp.op(pathA, pathB, Path.Op.INTERSECT);
                    pathResult.op(pathTemp, pathC, Path.Op.INTERSECT);
                    break;
                case A_UNION_B_UNION_C:
                    pathTemp.op(pathA, pathB, Path.Op.UNION);
                    pathResult.op(pathTemp, pathC, Path.Op.UNION);
                    break;
                case A_INTERSECT_B_MINUS_C:
                    pathTemp.op(pathA, pathB, Path.Op.INTERSECT);
                    pathResult.op(pathTemp, pathC, Path.Op.DIFFERENCE);
                    break;
                case ONLY_C_3SET:
                    pathTemp.op(pathA, pathB, Path.Op.UNION);
                    pathResult.op(pathC, pathTemp, Path.Op.DIFFERENCE);
                    break;
                default:
                    break;
            }

            canvas.drawPath(pathResult, shadePaint);
            canvas.drawPath(pathA, circleStrokePaint);
            canvas.drawPath(pathB, circleStrokePaint);
            canvas.drawPath(pathC, circleStrokePaint);

            textPaint.setTextSize(Math.max(9.5f * density, r * 0.40f));
            canvas.drawText("A", cxA - r * 0.65f, cyA - r * 0.65f, textPaint);
            canvas.drawText("B", cxB + r * 0.65f, cyB - r * 0.65f, textPaint);
            canvas.drawText("C", cxC + r * 0.85f, cyC + r * 0.75f, textPaint);
        }

        // Borde exterior del universo U
        canvas.drawPath(pathUniverse, borderPaint);
    }
}
