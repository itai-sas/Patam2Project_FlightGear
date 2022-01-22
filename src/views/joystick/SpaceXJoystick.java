package views.joystick;


import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;


public class SpaceXJoystick extends Region
{
    private static final double                    PREFERRED_WIDTH  = 500;
    private static final double                    PREFERRED_HEIGHT = 500;
    private static final double                    MINIMUM_WIDTH    = 50;
    private static final double                    MINIMUM_HEIGHT   = 50;
    private static final double                    MAXIMUM_WIDTH    = 1024;
    private static final double                    MAXIMUM_HEIGHT   = 1024;
    private static final double                    HALF_PI          = Math.PI / 2.0;
    private static final double                    MAX_STEP_SIZE    = 10;
    public              double                    size;
    public              double                    center;
    private              double                    width;
    private              double                    height;
    private              Canvas                    background;
    private              GraphicsContext           ctx;
    private              Circle                    touchIndicator;
    private              Circle                    touchPoint;
    private              Arc                       touchN;
    private              Arc                       touchNW;
    private              Arc                       touchW;
    private              Arc                       touchSW;
    private              Arc                       touchS;
    private              Arc                       touchSE;
    private              Arc                       touchE;
    private              Arc                       touchNE;
    private              Pane                      pane;
    private              LockState                 _lockState;
    private              ObjectProperty<LockState> lockState;
    private              boolean                   _stickyMode;
    private              BooleanProperty           stickyMode;
    private              boolean                   _animated;
    private              BooleanProperty           animated;
    private              long                      _durationMillis;
    private              LongProperty              durationMillis;
    private              double                    _stepSize;
    private              DoubleProperty            stepSize;
    private              boolean                   _stepButtonsVisible;
    private              BooleanProperty           stepButtonsVisible;
    private              Color                     _inactiveColor;
    private              ObjectProperty<Color>     inactiveColor;
    private              Color                     _activeColor;
    private              ObjectProperty<Color>     activeColor;
    private              Color                     _lockedColor;
    private              ObjectProperty<Color>     lockedColor;
    private              Color                     transclucentActiveColor;
    private              boolean                   _touched;
    private              BooleanProperty           touched;
    private              DoubleProperty            x;
    private              DoubleProperty            y;
    private              DoubleProperty            value;
    private              DoubleProperty            angle;
    private              double                    offsetX;
    private              double                    offsetY;
    private              Timeline                  timeline;
    private              EventHandler<MouseEvent>  mouseHandler;
    private              EventHandler<TouchEvent>  touchHandler;


    public SpaceXJoystick() {
        center                  = PREFERRED_WIDTH * 0.5;
        _lockState              = LockState.UNLOCKED;
        _stickyMode             = true;
        _animated               = true;
        _durationMillis         = 100;
        _stepSize               = 0.01;
        _stepButtonsVisible     = true;
        _inactiveColor          = Color.web("#506691");
        _activeColor            = Color.web("#CFF9FF");
        _lockedColor            = Color.web("#B36B6B");
        transclucentActiveColor = Color.color(_activeColor.getRed(), _activeColor.getGreen(), _activeColor.getBlue(), 0.25);
        _touched                = false;
        x                       = new DoublePropertyBase() {
            @Override protected void invalidated() {}
            @Override public Object getBean() { return SpaceXJoystick.this; }
            @Override public String getName() { return "valueX"; }
        };
        y                       = new DoublePropertyBase() {
            @Override protected void invalidated() {}
            @Override public Object getBean() { return SpaceXJoystick.this; }
            @Override public String getName() { return "valueY"; }
        };
        value                   = new DoublePropertyBase(0) {
            @Override protected void invalidated() { redraw(); }
            @Override public Object getBean() { return SpaceXJoystick.this; }
            @Override public String getName() { return "value"; }
        };
        angle                   = new DoublePropertyBase(0) {
            @Override protected void invalidated() {
            }
            @Override public Object getBean() { return SpaceXJoystick.this; }
            @Override public String getName() { return "angle"; }
        };
        offsetX                 = 0;
        offsetY                 = 0;

        initGraphics();
        registerListeners();
    }


    private void initGraphics() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 || Double.compare(getWidth(), 0.0) <= 0 ||
                Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        getStyleClass().add("touch-joystick");

        background = new Canvas(0.7 * PREFERRED_WIDTH, 0.7 * PREFERRED_HEIGHT);
        background.setMouseTransparent(true);
        ctx        = background.getGraphicsContext2D();

        touchN  = createArc(0);
        touchNW = createArc(45);
        touchW  = createArc(90);
        touchSW = createArc(135);
        touchS  = createArc(180);
        touchSE = createArc(225);
        touchE  = createArc(270);
        touchNE = createArc(315);

        touchIndicator = new Circle();
        touchIndicator.setFill(Color.TRANSPARENT);
        touchIndicator.setStroke(getInactiveColor());
        touchIndicator.setMouseTransparent(true);

        touchPoint = new Circle();
        touchPoint.setFill(getActiveColor());
        touchPoint.setStroke(getActiveColor());

        pane = new Pane(background, touchN, touchNW, touchW, touchSW, touchS, touchSE, touchE, touchNE, touchIndicator, touchPoint);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
    }


    @Override protected double computeMinWidth(final double HEIGHT) { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH) { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
    @Override protected double computeMaxWidth(final double HEIGHT) { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH) { return MAXIMUM_HEIGHT; }

    public void paint(){
        touchPoint.setFill(Color.BLACK);
        touchPoint.setStroke(getActiveColor());
        double maxR = size * 0.35;
        if ((Math.pow(getX(), 2.0)+Math.pow(getY(), 2.0))>1) {
        double Ang=Math.atan2(getY(), getX());
        double x=maxR*Math.cos(Ang);
        double y=maxR*Math.sin(Ang);
        touchPoint.setCenterX(x+center);
        touchPoint.setCenterY(-y+center);
        }

        else {
        touchPoint.setCenterX(getX()*maxR+center);
        touchPoint.setCenterY(-getY()*maxR+center);
        }
        drawBackground();
    }

    public LockState getLockState() { return null == lockState ? _lockState : lockState.get(); }
    public void setLockState(final LockState lockState) {
        if (null == this.lockState) {
            _lockState = lockState;
            switch(lockState) {
                case X_LOCKED:
                    touchN.setDisable(true);
                    touchNW.setDisable(true);
                    touchW.setDisable(false);
                    touchSW.setDisable(true);
                    touchS.setDisable(true);
                    touchSE.setDisable(true);
                    touchE.setDisable(false);
                    touchNE.setDisable(true);
                    break;
                case Y_LOCKED:
                    touchN.setDisable(false);
                    touchNW.setDisable(true);
                    touchW.setDisable(true);
                    touchSW.setDisable(true);
                    touchS.setDisable(false);
                    touchSE.setDisable(true);
                    touchE.setDisable(true);
                    touchNE.setDisable(true);
                    break;
                case UNLOCKED:
                default:
                    touchN.setDisable(false);
                    touchNW.setDisable(false);
                    touchW.setDisable(false);
                    touchSW.setDisable(false);
                    touchS.setDisable(false);
                    touchSE.setDisable(false);
                    touchE.setDisable(false);
                    touchNE.setDisable(false);
                    break;
            }
            redraw();
        } else {
            this.lockState.set(lockState);
        }
    }
    public ObjectProperty<LockState> lockStateProperty() {
        if (null == lockState) {
            lockState = new ObjectPropertyBase<>(_lockState) {
                @Override protected void invalidated() {
                    switch(get()) {
                        case X_LOCKED:
                            touchN.setDisable(false);
                            touchNW.setDisable(true);
                            touchW.setDisable(true);
                            touchSW.setDisable(true);
                            touchS.setDisable(false);
                            touchSE.setDisable(true);
                            touchE.setDisable(true);
                            touchNE.setDisable(true);
                            break;
                        case Y_LOCKED:
                            touchN.setDisable(false);
                            touchNW.setDisable(false);
                            touchW.setDisable(false);
                            touchSW.setDisable(false);
                            touchS.setDisable(false);
                            touchSE.setDisable(false);
                            touchE.setDisable(false);
                            touchNE.setDisable(false);
                            break;
                        case UNLOCKED:
                        default:
                            touchN.setDisable(false);
                            touchNW.setDisable(false);
                            touchW.setDisable(false);
                            touchSW.setDisable(false);
                            touchS.setDisable(false);
                            touchSE.setDisable(false);
                            touchE.setDisable(false);
                            touchNE.setDisable(false);
                            break;
                    }
                    redraw();
                }
                @Override public Object getBean() { return SpaceXJoystick.this; }
                @Override public String getName() { return "lockState"; }
            };
            _lockState = null;
        }
        return lockState;
    }

    public boolean isStickyMode() { return null == stickyMode ? _stickyMode : stickyMode.get(); }
    public void setStickyMode(final boolean stickyMode) {
        if (null == this.stickyMode) {
            _stickyMode = stickyMode;
        } else {
            this.stickyMode.set(stickyMode);
        }
    }
    public BooleanProperty stickyModeProperty() {
        if (null == stickyMode) {
            stickyMode = new BooleanPropertyBase(_stickyMode) {
                @Override public Object getBean() { return SpaceXJoystick.this; }
                @Override public String getName() { return "stickyMode"; }
            };
        }
        return stickyMode;
    }

    public boolean isAnimated() { return null == animated ? _animated : animated.get(); }
    public void setAnimated(final boolean animated) {
        if (null == this.animated) {
            _animated = animated;
        } else {
            this.animated.set(animated);
        }
    }
    public BooleanProperty animatedProperty() {
        if (null == animated) {
            animated = new BooleanPropertyBase(_animated) {
                @Override public Object getBean() { return SpaceXJoystick.this; }
                @Override public String getName() { return "animated"; }
            };
        }
        return animated;
    }

    public long getDurationMillis() { return null == durationMillis ? _durationMillis : durationMillis.get(); }
    public void setDurationMillis(final long durationMillis) {
        if (null == this.durationMillis) {
            _durationMillis = clamp(10, 1000, durationMillis);
        } else {
            this.durationMillis.set(durationMillis);
        }
    }
    public LongProperty durationMillisProperty() {
        if (null == durationMillis) {
            durationMillis = new LongPropertyBase(_durationMillis) {
                @Override protected void invalidated() { set(clamp(10, 1000, get())); }
                @Override public Object getBean() { return SpaceXJoystick.this; }
                @Override public String getName() { return "durationMillis"; }
            };
        }
        return durationMillis;
    }

    public double getStepSize() { return null == stepSize ? _stepSize : stepSize.get(); }
    public void setStepSize(final double stepSize) {
        if (null == this.stepSize) {
            _stepSize = clamp(0.001, MAX_STEP_SIZE, stepSize);
        } else {
            this.stepSize.set(stepSize);
        }
    }
    public DoubleProperty stepSizeProperty() {
        if (null == stepSize) {
            stepSize = new DoublePropertyBase(_stepSize) {
                @Override protected void invalidated() { set(clamp(0.001, MAX_STEP_SIZE, get())); }
                @Override public Object getBean() { return SpaceXJoystick.this; }
                @Override public String getName() { return "stepSizeX"; }
            };
        }
        return stepSize;
    }

    public boolean getStepButtonsVisible() { return null == stepButtonsVisible ? _stepButtonsVisible : stepButtonsVisible.get(); }
    public void setStepButtonsVisible(final boolean stepButtonsVisible) {
        if (null == this.stepButtonsVisible) {
            _stepButtonsVisible = stepButtonsVisible;
            touchN.setVisible(stepButtonsVisible);
            touchNW.setVisible(stepButtonsVisible);
            touchW.setVisible(stepButtonsVisible);
            touchSW.setVisible(stepButtonsVisible);
            touchS.setVisible(stepButtonsVisible);
            touchSE.setVisible(stepButtonsVisible);
            touchE.setVisible(stepButtonsVisible);
            touchNE.setVisible(stepButtonsVisible);
            redraw();
        } else {
            this.stepButtonsVisible.set(stepButtonsVisible);
        }
    }
    public BooleanProperty stepButtonsVisibleProperty() {
        if (null == stepButtonsVisible) {
            stepButtonsVisible = new BooleanPropertyBase(_stepButtonsVisible) {
                @Override protected void invalidated() {
                    touchN.setVisible(get());
                    touchNW.setVisible(get());
                    touchW.setVisible(get());
                    touchSW.setVisible(get());
                    touchS.setVisible(get());
                    touchSE.setVisible(get());
                    touchE.setVisible(get());
                    touchNE.setVisible(get());
                    redraw();
                }
                @Override public Object getBean() { return SpaceXJoystick.this; }
                @Override public String getName() { return "stepButtonsVisible"; }
            };
        }
        return stepButtonsVisible;
    }

    public Color getInactiveColor() { return null == inactiveColor ? _inactiveColor : inactiveColor.get(); }
    public void setInactiveColor(final Color inactiveColor) {
        if (null == this.inactiveColor) {
            _inactiveColor = inactiveColor;
            redraw();
        } else {
            this.inactiveColor.set(inactiveColor);
        }
    }
    public ObjectProperty<Color> inactiveColorProperty() {
        if (null == inactiveColor) {
            inactiveColor = new ObjectPropertyBase<Color>(_inactiveColor) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return SpaceXJoystick.this; }
                @Override public String getName() { return "inactiveColor"; }
            };
            _inactiveColor = null;
        }
        return inactiveColor;
    }

    public Color getActiveColor() { return null == activeColor ? _activeColor : activeColor.get(); }
    public void setActiveColor(final Color activeColor) {
        if (null == this.activeColor) {
            _activeColor            = activeColor;
            transclucentActiveColor = Color.color(_activeColor.getRed(), _activeColor.getGreen(), _activeColor.getBlue(), 0.25);
            redraw();
        } else {
            this.activeColor.set(activeColor);
        }
    }
    public ObjectProperty<Color> activeColorProperty() {
        if (null == activeColor) {
            activeColor = new ObjectPropertyBase<Color>(_activeColor) {
                @Override protected void invalidated() {
                    transclucentActiveColor = Color.color(get().getRed(), get().getGreen(), get().getBlue(), 0.25);
                    redraw();
                }
                @Override public Object getBean() { return SpaceXJoystick.this; }
                @Override public String getName() { return "activeColor"; }
            };
            _activeColor = null;
        }
        return activeColor;
    }

    public Color getLockedColor() { return null == lockedColor ? _lockedColor : lockedColor.get(); }
    public void setLockedColor(final Color lockedColor) {
        if (null == this.lockedColor) {
            _lockedColor = lockedColor;
            redraw();
        } else {
            this.lockedColor.set(lockedColor);
        }
    }
    public ObjectProperty<Color> lockedColorProperty() {
        if (null == lockedColor) {
            lockedColor = new ObjectPropertyBase<Color>(_lockedColor) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return SpaceXJoystick.this; }
                @Override public String getName() { return "lockedColor"; }
            };
            _lockedColor = null;
        }
        return lockedColor;
    }

    public boolean isTouched() { return null == touched ? _touched : touched.get(); }
    private void setTouched(final boolean touched) {
        if (null == this.touched) {
            _touched = touched;
            redraw();
        } else {
            this.touched.set(touched);
        }
    }
    public ReadOnlyBooleanProperty touchedProperty() {
        if (null == touched) {
            touched = new BooleanPropertyBase(_touched) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return SpaceXJoystick.this; }
                @Override public String getName() { return "touched"; }
            };
        }
        return touched;
    }

    public double getValue() { return value.get(); }
    private void setValue(final double value) { this.value.set(value); }
    public DoubleProperty valueProperty() { return value; }

    public double getAngle() { return angle.get(); }
    private void setAngle(final double angle) { this.angle.set(angle); }
    public DoubleProperty angleProperty() { return angle; }

    public double getX() { return x.get(); }
    private void setX(final double x) { this.x.set(x); }
    public DoubleProperty xProperty() { return x; }

    public double getY() { return y.get(); }
    private void setY(final double y) { this.y.set(y); }
    public DoubleProperty yProperty() { return y; }

    private void reset() {
        if (!isStickyMode()) {
            if (isAnimated()) {
                KeyValue kvX0 = new KeyValue(touchPoint.centerXProperty(), touchPoint.getCenterX(), Interpolator.EASE_OUT);
                KeyValue kvY0 = new KeyValue(touchPoint.centerYProperty(), touchPoint.getCenterY(), Interpolator.EASE_OUT);
                KeyValue kvV0 = new KeyValue(value, value.get(), Interpolator.EASE_OUT);
                KeyValue kvX1 = new KeyValue(touchPoint.centerXProperty(), 0.5 * size, Interpolator.EASE_OUT);
                KeyValue kvY1 = new KeyValue(touchPoint.centerYProperty(), 0.5 * size, Interpolator.EASE_OUT);
                KeyValue kvV1 = new KeyValue(value, 0, Interpolator.EASE_OUT);
                KeyFrame kf0  = new KeyFrame(Duration.ZERO, kvX0, kvY0, kvV0);
                KeyFrame kf1  = new KeyFrame(Duration.millis(getDurationMillis()), kvX1, kvY1, kvV1);
                timeline.getKeyFrames().setAll(kf0, kf1);
                timeline.play();
            } else {
                touchPoint.setCenterX(center);
                touchPoint.setCenterY(center);
                resetTouchButtons();
                value.set(0);
                angle.set(0);
            }
        }
    }

    private void resetTouchButtons() {
        Color inactiveColor = getInactiveColor();
        switch(getLockState()) {
            case X_LOCKED:
                touchN.setStroke(transclucentActiveColor);
                touchNW.setStroke(transclucentActiveColor);
                touchW.setStroke(inactiveColor);
                touchSW.setStroke(transclucentActiveColor);
                touchS.setStroke(transclucentActiveColor);
                touchSE.setStroke(transclucentActiveColor);
                touchE.setStroke(inactiveColor);
                touchNE.setStroke(transclucentActiveColor);
                break;
            case Y_LOCKED:
                touchN.setStroke(inactiveColor);
                touchNW.setStroke(transclucentActiveColor);
                touchW.setStroke(transclucentActiveColor);
                touchSW.setStroke(transclucentActiveColor);
                touchS.setStroke(inactiveColor);
                touchSE.setStroke(transclucentActiveColor);
                touchE.setStroke(transclucentActiveColor);
                touchNE.setStroke(transclucentActiveColor);
                break;
            case UNLOCKED:
            default:
                touchN.setStroke(inactiveColor);
                touchNW.setStroke(inactiveColor);
                touchW.setStroke(inactiveColor);
                touchSW.setStroke(inactiveColor);
                touchS.setStroke(inactiveColor);
                touchSE.setStroke(inactiveColor);
                touchE.setStroke(inactiveColor);
                touchNE.setStroke(inactiveColor);
                break;
        }
    }

    private void setXY(final double newX, final double newY, final double newAngle) {
        double x   = clamp(size * 0.15, size * 0.85, newX);
        double y   = clamp(size * 0.15, size * 0.85, newY);
        double dx  = x - center;
        double dy  = -(y - center);
        double rad = Math.atan2(dy, dx) + HALF_PI;
        double phi = Math.toDegrees(rad - Math.PI);
        if (phi < 0) { phi += 360.0; }
        setAngle(phi);
        double r    = Math.sqrt(dx * dx + dy * dy);
        double maxR = size * 0.35;
        if (r > maxR) {
            x = -Math.cos(rad + HALF_PI) * maxR + center;
            y = Math.sin(rad + HALF_PI) * maxR + center;
            r = maxR;
        }
        setX(-Math.cos(rad + HALF_PI));
        setY(-Math.sin(rad + HALF_PI));

        touchPoint.setCenterX(x);
        touchPoint.setCenterY(y);
        setValue(r / maxR);

        reset();
    }

    private Arc createArc(final double startAngle) {
        Arc arc  = new Arc(0.5 * size, 0.5 * size, 0.455 * size, 0.455 * size, startAngle + 90 - 18.5, 37);
        arc.setFill(Color.TRANSPARENT);
        arc.setStrokeLineCap(StrokeLineCap.BUTT);
        return arc;
    }

    public double clamp(final double min, final double max, final double value) {
        if (value < min) { return min; }
        if (value > max) { return max; }
        return value;
    }
    private long clamp(final long min, final long max, final long value) {
        if (value < min) { return min; }
        if (value > max) { return max; }
        return value;
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size   = width < height ? width : height;
        center = size * 0.5;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            pane.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);

            background.setWidth(0.7 * size);
            background.setHeight(0.7 * size);
            background.relocate(0.15 *size, 0.15 * size);

            resizeArc(touchN);
            resizeArc(touchNW);
            resizeArc(touchW);
            resizeArc(touchSW);
            resizeArc(touchS);
            resizeArc(touchSE);
            resizeArc(touchE);
            resizeArc(touchNE);

            touchIndicator.setRadius(0.4 * size);
            touchIndicator.setCenterX(center);
            touchIndicator.setCenterY(center);

            touchPoint.setRadius(0.05 * size);
            touchPoint.setCenterX(center + offsetX);
            touchPoint.setCenterY(center + offsetY);

            redraw();
        }
    }

    private void resizeArc(final Arc arc) {
        arc.setCenterX(center);
        arc.setCenterY(center);
        arc.setRadiusX(0.455 * size);
        arc.setRadiusY(0.455 * size);
        arc.setStrokeWidth(0.084 * size);
    }

    private void drawBackground() {
        double w = background.getWidth();
        double h = background.getHeight();
        ctx.clearRect(0, 0, background.getWidth(), background.getHeight());
        ctx.setFill(getInactiveColor());
        ctx.fillOval(0, 0, w, h);
        ctx.setFill(Color.TRANSPARENT);
        ctx.setStroke(LockState.X_LOCKED == getLockState() || LockState.Y_LOCKED == getLockState() ? getLockedColor() : transclucentActiveColor);
        ctx.strokeLine(0.15 * w, 0.15 * h, 0.85 * w, 0.85 * h);
        ctx.strokeLine(0.85 * w, 0.15 * h, 0.15 * w, 0.85 * h);
        ctx.setStroke(transclucentActiveColor);
        ctx.strokeOval(0.42857143 * w, 0.42857143 * h, 0.14285714 * w, 0.14285714 * h);
        ctx.setStroke(LockState.Y_LOCKED == getLockState() ? getLockedColor() : transclucentActiveColor);
        ctx.strokeLine(0, 0.5 * h, w, 0.5 *h);
        ctx.setStroke(LockState.X_LOCKED == getLockState() ? getLockedColor() : transclucentActiveColor);
        ctx.strokeLine(0.5 * w, 0, 0.5 * w, h);

        ctx.save();
        double value            = getValue();
        double chevronHalfWidth = 0.05 * w;
        double chevronHeight    = 0.04 * h;
        double center           = 0.5 * w;
        double offsetY          = h - chevronHeight * 0.25;
        double chevronStepY     = 1.22 * chevronHeight;
        ctx.translate(center, center);
        ctx.rotate(-getAngle());
        ctx.translate(-center, -center);
        ctx.setStroke(getActiveColor());
        ctx.setLineWidth(0.015 * h);
        ctx.setLineCap(StrokeLineCap.ROUND);
        ctx.setLineJoin(StrokeLineJoin.ROUND);
        int counter = 0;
        for (double i = 0.0 ; i < value - 0.1 ; i += 0.1) {
            ctx.strokeLine(center - chevronHalfWidth, offsetY - counter * chevronStepY, center, offsetY - (counter + 1) * chevronStepY);
            ctx.strokeLine(center, offsetY - (counter + 1) * chevronStepY, center + chevronHalfWidth, offsetY - counter * chevronStepY);
            counter += 1;
        }
        ctx.restore();
    }

    private void redraw() {
        drawBackground();
        Color activeColor   = getActiveColor();
        Color inactiveColor = getInactiveColor();
        switch(getLockState()) {
            case X_LOCKED:
                touchN.setStroke(transclucentActiveColor);
                touchNW.setStroke(transclucentActiveColor);
                touchW.setStroke(touchW.isHover() ? activeColor : inactiveColor);
                touchSW.setStroke(transclucentActiveColor);
                touchS.setStroke(transclucentActiveColor);
                touchSE.setStroke(transclucentActiveColor);
                touchE.setStroke(touchE.isHover() ? activeColor : inactiveColor);
                touchNE.setStroke(transclucentActiveColor);
                break;
            case Y_LOCKED:
                touchN.setStroke(touchN.isHover() ? activeColor : inactiveColor);
                touchNW.setStroke(transclucentActiveColor);
                touchW.setStroke(transclucentActiveColor);
                touchSW.setStroke(transclucentActiveColor);
                touchS.setStroke(touchS.isHover() ? activeColor : inactiveColor);
                touchSE.setStroke(transclucentActiveColor);
                touchE.setStroke(transclucentActiveColor);
                touchNE.setStroke(transclucentActiveColor);
                break;
            case UNLOCKED:
            default:
                touchN.setStroke(touchN.isHover() ? activeColor : inactiveColor);
                touchNW.setStroke(touchNW.isHover() ? activeColor : inactiveColor);
                touchW.setStroke(touchW.isHover() ? activeColor : inactiveColor);
                touchSW.setStroke(touchSW.isHover() ? activeColor : inactiveColor);
                touchS.setStroke(touchS.isHover() ? activeColor : inactiveColor);
                touchSE.setStroke(touchSE.isHover() ? activeColor : inactiveColor);
                touchE.setStroke(touchE.isHover() ? activeColor : inactiveColor);
                touchNE.setStroke(touchNE.isHover() ? activeColor : inactiveColor);
                break;
        }

        touchPoint.setFill(Color.BLACK);
        touchPoint.setRadius(size*0.05);
        touchPoint.setStroke(activeColor);
        touchIndicator.setStroke(inactiveColor);
    }
}