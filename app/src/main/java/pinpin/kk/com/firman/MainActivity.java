package pinpin.kk.com.firman;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.security.SecureRandom;

public class MainActivity extends AppCompatActivity {
    private static final int PANEL_ROW = 3;//行
    private static final int PANEL_COLUMN = 5;//列
    private static final int DEFAULT_PADDING = 2;//邊距
    private static final long DEFAULT_ANIM_DURATION = 1000;//移動時間
    private static final SecureRandom random = new SecureRandom();
    private static final int DEFAULT_SWAP_NUM = 15;//打亂次數

    private boolean isAnimRunning;//動畫進行

    private ImageView[][] mGamePics;//方塊陣列

    private ImageView mBlankImageView;//空白方塊

    private GestureDetector mDetector;//手勢判斷
    private enum DIRECTION {
        LEFT, RIGHT, TOP, BOTTOM
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();//陣列資料
        initView();//視圖
    }

    private void initData() {
        //建立陣列
        mGamePics = new ImageView[PANEL_ROW][PANEL_COLUMN];
        //取得方塊寬、高
        Bitmap srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.huoying);
        int squareWidth = srcBitmap.getWidth() / PANEL_COLUMN;
        int squareHeight = srcBitmap.getHeight() / PANEL_ROW;
        int x, y;
        //陣列設定
        for (int i = 0; i < PANEL_ROW; i++) {
            for (int j = 0; j < PANEL_COLUMN; j++) {
                x = j * squareWidth;//起始x座標
                y = i * squareHeight;//起始y座標

                Bitmap square = Bitmap.createBitmap(srcBitmap, x, y, squareWidth, squareHeight);
                final ImageView imageView = new ImageView(this);
                imageView.setImageBitmap(square);
                imageView.setPadding(DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING,
                        DEFAULT_PADDING);
                GameInfo gameInfo = new GameInfo(x, x + squareWidth, y, y + squareHeight, square, i, j);
                imageView.setTag(gameInfo);//儲存方塊資料
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isAvail = isAvailableBlankImageView(imageView);

                        if (isAvail) {
                            animTranslation(imageView);
                        }
                    }
                });
                mGamePics[i][j] = imageView;
            }
        }



        //建立手勢判斷
        mDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (!isAnimRunning) {
                    DIRECTION direction = getDirection(e1.getX(), e1.getY(), e2.getX(), e2.getY());
                    swapImgsByDirection(direction, true);
                }
                return true;
            }
        });
    }

    //建立視圖於Gridlayout
    private void initView() {
        GridLayout mGridLayout = (GridLayout) findViewById(R.id.id_gridlayout);
        if (mGridLayout != null) {
            for (ImageView[] mGamePic : mGamePics) {
                for (ImageView iv : mGamePic) {
                    mGridLayout.addView(iv);
                }
            }
        }
        //
        setBlankImageView(mGamePics[PANEL_ROW - 1][PANEL_COLUMN - 1]);

        //隨機打亂順序
        for (int i = 0; i < DEFAULT_SWAP_NUM; i ++) {
            DIRECTION randomDirection = randowmEnum(DIRECTION.class);
            swapImgsByDirection(randomDirection, false);
        }
    }

    private void setBlankImageView(ImageView iv) {
        GameInfo gameInfo = (GameInfo) iv.getTag();
        gameInfo.setBitmap(null);
        iv.setImageBitmap(null);
        iv.setTag(gameInfo);
        mBlankImageView = iv;
    }

    /**
     * 判別鄰近是否為空白圖片
     */
    private boolean isAvailableBlankImageView(ImageView iv) {
        GameInfo ivInfo = (GameInfo) iv.getTag();
        GameInfo blankInfo = (GameInfo) mBlankImageView.getTag();

        if (ivInfo.bottomY == blankInfo.bottomY && ivInfo.rightX == blankInfo.leftX) {
            // 左邊
            return true;
        } else if (ivInfo.bottomY == blankInfo.bottomY && ivInfo.leftX == blankInfo.rightX) {
            // 右邊
            return true;
        } else if (ivInfo.leftX == blankInfo.leftX && ivInfo.bottomY == blankInfo.topY) {
            // 上邊
            return true;
        } else if (ivInfo.leftX == blankInfo.leftX && ivInfo.topY == blankInfo.bottomY) {
            // 下邊
            return true;
        }

        return false;
    }

    /**
     * 獲取手勢方向
     */
    private DIRECTION getDirection(float x1, float y1, float x2, float y2) {
        boolean isLeftOrRight = Math.abs(x1 - x2) > Math.abs(y1 - y2);
        if (isLeftOrRight) {
            return x1 - x2 > 0 ? DIRECTION.LEFT : DIRECTION.RIGHT;
        } else {
            return y1 - y2 > 0 ? DIRECTION.TOP : DIRECTION.BOTTOM;
        }
    }

    /**
     * 以手勢方向來移動圖片
     */
    private void swapImgsByDirection(DIRECTION direction, boolean useAnim) {
        GameInfo blankGameInfo = (GameInfo) mBlankImageView.getTag();
        int locy = blankGameInfo.locRow, locx = blankGameInfo.locCol;
        switch (direction) {
            case LEFT:
                locx = blankGameInfo.locCol + 1;
                break;
            case RIGHT:
                locx = blankGameInfo.locCol - 1;
                break;
            case TOP:
                locy = blankGameInfo.locRow + 1;
                break;
            case BOTTOM:
                locy = blankGameInfo.locRow - 1;
                break;
        }

        if (locx >= 0 && locx < PANEL_COLUMN && locy >= 0 && locy < PANEL_ROW) {
            if (useAnim) {
                animTranslation(mGamePics[locy][locx]);
            } else {
                directTranslation(mGamePics[locy][locx]);
            }
        }
    }

    /**
     * 有動畫效果的圖片移動
     */
    private void animTranslation(final ImageView iv) {
        final GameInfo ivInfo = (GameInfo) iv.getTag();
        final GameInfo blankInfo = (GameInfo) mBlankImageView.getTag();
        TranslateAnimation anim = null;



        if (ivInfo.bottomY == blankInfo.bottomY) {
            // 左邊or右邊
            anim = new TranslateAnimation(0, blankInfo.leftX - ivInfo.leftX, 0, 0);
        } else if (ivInfo.leftX == blankInfo.leftX) {
            // 上边邊or下邊
            anim = new TranslateAnimation(0, 0, 0, blankInfo.topY - ivInfo.topY);
        }

        assert anim != null;
        anim.setDuration(DEFAULT_ANIM_DURATION);
        anim.setFillAfter(true);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimRunning = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                iv.clearAnimation();
                swapImages(ivInfo, blankInfo, iv);
                isAnimRunning = false;
                if (isGameOver()) {
                    Toast.makeText(MainActivity.this, "You Win!!!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        iv.startAnimation(anim);
    }

    /**
     * 無動畫效果的圖片移動
     */
    private void directTranslation(ImageView iv) {
        final GameInfo ivInfo = (GameInfo) iv.getTag();
        final GameInfo blankInfo = (GameInfo) mBlankImageView.getTag();
        swapImages(ivInfo, blankInfo, iv);
    }

    private void swapImages(GameInfo ivInfo, GameInfo blankInfo, ImageView iv) {
        swapCoordinateInfo(ivInfo, blankInfo);
        swapLocationInfo(ivInfo, blankInfo);

        iv.setTag(blankInfo);
        mBlankImageView.setTag(ivInfo);

        mBlankImageView.setImageBitmap(ivInfo.getBitmap());
        iv.setImageBitmap(blankInfo.getBitmap());

        mBlankImageView = iv;
    }

    /**
     * 交換上下界線資料
     */
    private void swapLocationInfo(GameInfo ivInfo, GameInfo blankInfo) {
        int blankLocCol = blankInfo.locCol;
        int blankLocRow = blankInfo.locRow;
        blankInfo.setNewLoc(ivInfo.locRow, ivInfo.locCol);
        ivInfo.setNewLoc(blankLocRow, blankLocCol);
    }

    /**
     * 交換座標資料.
     */
    private void swapCoordinateInfo(GameInfo ivInfo, GameInfo blankInfo) {
        int ivLeftX = ivInfo.leftX, ivRightX = ivInfo.rightX,
                ivTopY = ivInfo.topY, ivBottomY = ivInfo.bottomY;

        ivInfo.setCoordinate(blankInfo.leftX, blankInfo.topY, blankInfo.rightX, blankInfo.bottomY);
        blankInfo.setCoordinate(ivLeftX, ivTopY, ivRightX, ivBottomY);
    }

    /**
     * 判斷遊戲是否結束
     */
    private boolean isGameOver() {
        for (ImageView[] mGamePic : mGamePics) {
            for (ImageView aMGamePic : mGamePic) {
                GameInfo gameInfo = (GameInfo) aMGamePic.getTag();
                if (!gameInfo.isCorrectPic()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    private static class GameInfo {
        private int leftX;
        private int topY;
        private int rightX;
        private int bottomY;
        private Bitmap bitmap;
        private int locRow;
        private int locCol;
        private int initRow;
        private int initCol;

        public GameInfo(int leftX, int rightX, int topY, int bottomY, Bitmap bitmap, int row, int col) {
            this.leftX = leftX;
            this.rightX = rightX;
            this.topY = topY;
            this.bottomY = bottomY;
            this.bitmap = bitmap;//square
            this.locRow = row;
            this.locCol = col;
            this.initRow = row;
            this.initCol = col;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap mBitmap) {
            this.bitmap = mBitmap;
        }

        public boolean isCorrectPic() {
            return locRow == initRow && locCol == initCol;
        }

        public void setNewLoc(int row, int col) {
            locRow = row;
            locCol = col;
        }

        public void setCoordinate(int leftX, int topY, int rightX, int bottomY) {
            this.leftX = leftX;
            this.topY = topY;
            this.rightX = rightX;
            this.bottomY = bottomY;
        }
    }

    public static <T extends Enum<?>> T randowmEnum(Class<T> clazz) {
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }
}
