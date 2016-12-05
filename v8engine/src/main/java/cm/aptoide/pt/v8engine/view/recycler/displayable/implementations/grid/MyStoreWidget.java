package cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cm.aptoide.accountmanager.AptoideAccountManager;
import cm.aptoide.pt.imageloader.ImageLoader;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.V8Engine;
import cm.aptoide.pt.v8engine.interfaces.FragmentShower;
import cm.aptoide.pt.v8engine.util.StoreThemeEnum;
import cm.aptoide.pt.v8engine.view.recycler.widget.Widget;
import com.jakewharton.rxbinding.view.RxView;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by trinkes on 05/12/2016.
 */

public class MyStoreWidget extends Widget<MyStoreDisplayable> {

  private LinearLayout widgetLayout;
  private ImageView storeIcon;
  private TextView storeName;
  private Button exploreButton;
  private CompositeSubscription subscriptions;

  public MyStoreWidget(View itemView) {
    super(itemView);
    subscriptions = new CompositeSubscription();
  }

  @Override protected void assignViews(View itemView) {
    widgetLayout = (LinearLayout) itemView.findViewById(R.id.widgetLayout);
    storeIcon = (ImageView) itemView.findViewById(R.id.store_icon);
    storeName = (TextView) itemView.findViewById(R.id.store_name);
    exploreButton = (Button) itemView.findViewById(R.id.explore_button);
  }

  @Override public void bindView(MyStoreDisplayable displayable) {

    FragmentActivity context = getContext();
    @ColorInt int color = getColorOrDefault(displayable.getTheme(), context);
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      Drawable d = context.getDrawable(R.drawable.dialog_bg_2);
      d.setColorFilter(color, PorterDuff.Mode.SRC_IN);
      widgetLayout.setBackground(d);
    } else {
      Drawable d = context.getResources().getDrawable(R.drawable.dialog_bg_2);
      d.setColorFilter(color, PorterDuff.Mode.SRC_IN);
      widgetLayout.setBackgroundDrawable(d);
    }
    exploreButton.setTextColor(color);

    ImageLoader.loadWithShadowCircleTransform(
        AptoideAccountManager.getUserData().getUserAvatarRepo(), storeIcon);

    storeName.setText(AptoideAccountManager.getUserData().getUserRepo());
    RxView.clicks(exploreButton)
        .subscribe(click -> ((FragmentShower) context).pushFragmentV4(V8Engine.getFragmentProvider()
            .newStoreFragment(AptoideAccountManager.getUserData().getUserRepo())));
  }

  @Override public void unbindView() {
    super.unbindView();
    subscriptions.clear();
  }

  private int getColorOrDefault(StoreThemeEnum theme, Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return context.getResources().getColor(theme.getStoreHeader(), context.getTheme());
    } else {
      return context.getResources().getColor(theme.getStoreHeader());
    }
  }
}
