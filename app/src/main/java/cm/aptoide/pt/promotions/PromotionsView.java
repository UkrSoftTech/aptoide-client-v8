package cm.aptoide.pt.promotions;

import cm.aptoide.pt.presenter.View;
import rx.Observable;

public interface PromotionsView extends View {

  void showPromotionApp(PromotionViewApp promotionViewApp);

  Observable<PromotionViewApp> installButtonClick();

  Observable<Boolean> showRootInstallWarningPopup();
}