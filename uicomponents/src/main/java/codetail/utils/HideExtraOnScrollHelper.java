package codetail.utils;

public class HideExtraOnScrollHelper{
    public final static int UNKNOWN = -1;
    public final static int TOP = 0;
    public final static int BOTTOM = 1;

    int mDraggedAmount;
    int mOldDirection;
    int mDragDirection;

    final int mMinFlingDistance;

    public HideExtraOnScrollHelper(int minFlingDistance) {
        mOldDirection  =
        mDragDirection =
        mDraggedAmount = UNKNOWN;

        mMinFlingDistance = minFlingDistance;
    }

    /**
     * Checks need to hide extra objects on scroll or not
     *
     * @param dy scrolled distance y
     * @return true if need to hide extra objects on screen
     */
    public boolean isObjectsShouldBeOutside(int dy){
        mDragDirection = dy > 0 ? BOTTOM : TOP;

        if(mDragDirection != mOldDirection){
            mDraggedAmount = 0;
        }

        mDraggedAmount += dy;
        boolean shouldBeOutside = false;

        if(mDragDirection == TOP && Math.abs(mDraggedAmount) > mMinFlingDistance){
            shouldBeOutside = false;
        }else if(mDragDirection == BOTTOM && mDraggedAmount > mMinFlingDistance){
            shouldBeOutside = true;
        }

        if(mOldDirection != mDragDirection){
            mOldDirection = mDragDirection;
        }

        return shouldBeOutside;
    }
}
