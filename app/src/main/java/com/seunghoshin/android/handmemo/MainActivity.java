package com.seunghoshin.android.handmemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    FrameLayout layout;
    RadioGroup color; // 색상조절 옵션
    Board board; // 그림판
    SeekBar stroke; // 두께 조절옵션
    ImageView imageView; //캡쳐한 이미지를 썸네일로 화면에 표시

    // 캡쳐한 이미지를 저장하는 변수
    Bitmap captured = null;

    // 브러쉬는 값을 조절할때마다 그림판에 새로 생성됨

    static int progress;
    int colorType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // 그림판이 들어가있는 레이아웃
        layout = (FrameLayout) findViewById(R.id.layout);
        // 색상 선택
        color = (RadioGroup) findViewById(R.id.color);
        // color의 버튼을 활성화 시킨다
        color.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.rdGreen:
                        colorType = Color.GREEN;
                        break;
                    case R.id.rdBlue:
                        colorType = Color.BLUE;
                        break;
                    case R.id.rdRed:
                        colorType = Color.RED;
                        break;
                    case R.id.rdErase:
                        colorType = Color.WHITE;
                        break;

                }
                setBrush();
            }
        });


        // 두께 선택

        stroke = (SeekBar) findViewById(R.id.stroke);
        stroke.setProgress(10);
        stroke.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MainActivity.progress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setBrush();

            }
        });


        // 썸네일 이미지 뷰
        imageView = (ImageView) findViewById(R.id.imageView);
        // 캡쳐할 뷰의 캐쉬를 사용한다
        // 저장버튼
        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 드로잉 캐쉬를 지워주고
                layout.destroyDrawingCache();
                // 다시 만들고
                layout.buildDrawingCache();
                // 레이아웃의 그려진 내용을 Bitmap 형태로 가져온다.
                captured = layout.getDrawingCache();
                // 캡쳐한 이미지를 썸네일에 보여준다.
                imageView.setImageBitmap(captured);
            }
        });


        // 1. 보드를 새로 생성한다.
        board = new Board(getBaseContext());

        // 2. 생성된 보드를 FrameLayout 화면에 세팅한다.
        layout.addView(board);

        // 3. 기본적인 색깔을 초기에 설정해준다
        colorType = Color.BLUE;
        setBrush();

    }



    private void setBrush() {
        // 2. 붓을 만들어서 보드에 담는다
        Paint paint = new Paint();
        paint.setColor(colorType);
        // 매끄럽게 만들어준다
        paint.setStyle(Paint.Style.STROKE);
        // 선을 채우지 않고 굵기를 지정
        paint.setStyle(Paint.Style.STROKE);
        //선을 그어줄때 부드럽게 만들어주는 역활
        paint.setAntiAlias(true);
        // 각진곳을 라운드 처리한다
        paint.setStrokeJoin(Paint.Join.ROUND);
        // 각진곳을 이어주면서 처리한다
        paint.setStrokeCap(Paint.Cap.ROUND);
        // 옆으로 꺠진것(찌글찌글), 선의 노이즈를 보정해준다
        paint.setDither(true);
        // 선의 굵기
        paint.setStrokeWidth(progress);

        // 보드에 paint를 지정해준다
        board.setPaint(paint);

    }



    class Brush {
        Paint paint;
        Path path;

    }

    class Board extends View {
        Paint paint;

        List<Brush> brushes = new ArrayList<>();
        Path current_path;


        public Board(Context context) {
            super(context);
//            path = new Path(); //TODO 왜 주석처리가 되는지 , 위에서 참조를 안하기 떄문에
        }


        public void setPaint(Paint paint) {
            this.paint = paint;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            for (Brush brush : brushes) {
                canvas.drawPath(brush.path, brush.paint);
            }
        }


        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // 내가 터치한 좌표를 꺼낸다
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                // 터치가 시작되면 Path를 생성하고 List에 담아둔다
                case MotionEvent.ACTION_DOWN:
                    // 새로운 붓을 생성 - path 와 paint를 포함
                    Brush brush = new Brush();
                    // 가. 패스생성
                    current_path = new Path();
                    // 나. 생성한 패스와 현재 페이트를 브러쉬에 담는다
                    brush.path = current_path;
                    brush.paint = paint;

                    // 다. 완성된 브러쉬를 배열에 담아준다
                    brushes.add(brush);

                    Log.e("LOG", "onTouchEvent=================down");
                    current_path.moveTo(x, y); // 이전점과 현재점 사이를 그리지 않고 이동한다.
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.e("LOG", "onTouchEvent==================move");
                    current_path.lineTo(x, y); // 바로 이전점과 현재점사이에 줄을 그어준다.
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    Toast.makeText(getContext(), "언제찍히니?", Toast.LENGTH_SHORT).show();
                    break;
                case MotionEvent.ACTION_UP:
                    current_path.lineTo(x, y); // 손을 땟을때 뭐가 되는지
                    break;
            }

            // Path 를 그린후 화면을 갱신해서 반영해 준다.
            invalidate();

            // 리턴 false 일 경우 touch 이벤트를 연속해서 발생시키지 않는다.
            // 즉, 드래그시 onTouchEvent 가 호출되지 않는다
            // TODO return true 이유 // true를 안할 경우 화면을 떼면 이어서 못그린다
            return true;
        }
    }
}

