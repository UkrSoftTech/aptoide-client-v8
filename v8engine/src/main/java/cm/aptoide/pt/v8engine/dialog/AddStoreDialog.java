/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 17/06/2016.
 */

package cm.aptoide.pt.v8engine.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cm.aptoide.accountmanager.AptoideAccountManager;
import cm.aptoide.pt.dataprovider.DataProvider;
import cm.aptoide.pt.dataprovider.exception.AptoideWsV7Exception;
import cm.aptoide.pt.dataprovider.repository.IdsRepositoryImpl;
import cm.aptoide.pt.dataprovider.ws.v7.store.GetStoreMetaRequest;
import cm.aptoide.pt.interfaces.AptoideClientUUID;
import cm.aptoide.pt.model.v7.BaseV7Response;
import cm.aptoide.pt.navigation.NavigationManagerV4;
import cm.aptoide.pt.preferences.secure.SecurePreferencesImplementation;
import cm.aptoide.pt.utils.AptoideUtils;
import cm.aptoide.pt.utils.GenericDialogs;
import cm.aptoide.pt.utils.design.ShowMessage;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.V8Engine;
import cm.aptoide.pt.v8engine.activity.StoreSearchActivity;
import cm.aptoide.pt.v8engine.util.StoreUtils;
import cm.aptoide.pt.v8engine.util.StoreUtilsProxy;
import cm.aptoide.pt.v8engine.websocket.StoreAutoCompleteWebSocket;
import com.jakewharton.rxbinding.view.RxView;
import rx.subscriptions.CompositeSubscription;

/**
 * Created with IntelliJ IDEA. User: rmateus Date: 18-10-2013 Time: 17:27 To change this template
 * use File | Settings |
 * File Templates.
 */
public class AddStoreDialog extends DialogFragment {

  private static String STORE_WEBSOCKET_PORT = "9002";
  private static StoreAutoCompleteWebSocket storeAutoCompleteWebSocket;
  private final int PRIVATE_STORE_REQUEST_CODE = 20;
  private final AptoideClientUUID aptoideClientUUID;
  private NavigationManagerV4 navigationManager;
  private String storeName;
  private Dialog loadingDialog;
  private CompositeSubscription mSubscriptions;
  private SearchView searchView;
  private Button addStoreButton;
  private LinearLayout topStoresButton;
  private TextView topStoreText1;
  private TextView topStoreText2;
  private String givenStoreName;

  public AddStoreDialog() {
    aptoideClientUUID = new IdsRepositoryImpl(SecurePreferencesImplementation.getInstance(),
        DataProvider.getContext());
  }

  public void attachFragmentManager(NavigationManagerV4 navigationManager) {
    this.navigationManager = navigationManager;
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == PRIVATE_STORE_REQUEST_CODE) {
      switch (resultCode) {
        case Activity.RESULT_OK:
          dismiss();
          break;
      }
    }
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    if (getDialog() != null) {
      getDialog().getWindow().setTitle(getString(R.string.subscribe_store));
    }

    return inflater.inflate(R.layout.dialog_add_store, container, false);
  }

  @Override public void onViewCreated(final View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    bindViews(view);
    setupSearchView(view);
    setupStoreSearch(searchView);
    mSubscriptions.add(RxView.clicks(addStoreButton).subscribe(click -> {
      if (givenStoreName == null) {
        givenStoreName = searchView.getQuery().toString();
      }
      if (givenStoreName.length() > 0) {
        AddStoreDialog.this.storeName = givenStoreName;
        AptoideUtils.SystemU.hideKeyboard(getActivity());
        getStore(givenStoreName);
        showLoadingDialog();
      }
    }));
    mSubscriptions.add(RxView.clicks(topStoresButton).subscribe(click -> topStoresAction()));
    mSubscriptions.add(RxView.clicks(topStoreText1).subscribe(click -> topStoresAction()));
    mSubscriptions.add(RxView.clicks(topStoreText2).subscribe(click -> topStoresAction()));
  }

  private void bindViews(View view) {
    searchView = (SearchView) view.findViewById(R.id.edit_store_uri);
    addStoreButton = (Button) view.findViewById(R.id.button_dialog_add_store);
    topStoresButton = (LinearLayout) view.findViewById(R.id.button_top_stores);
    topStoreText1 = (TextView) view.findViewById(R.id.top_stores_text_1);
    topStoreText2 = (TextView) view.findViewById(R.id.top_stores_text_2);
  }

  private void setupSearchView(View view) {
    searchView.setIconifiedByDefault(false);
    ImageView image = ((ImageView) view.findViewById(R.id.search_mag_icon));
    image.setImageDrawable(null);
  }

  private void setupStoreSearch(SearchView searchView) {
    final SearchManager searchManager =
        (SearchManager) V8Engine.getContext().getSystemService(Context.SEARCH_SERVICE);
    ComponentName cn = new ComponentName(V8Engine.getContext(), StoreSearchActivity.class);
    searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));

    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override public boolean onQueryTextSubmit(String query) {
        //TODO: add store button action
        return false;
      }

      @Override public boolean onQueryTextChange(String newText) {
        return false;
      }
    });

    searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
      @Override public boolean onSuggestionSelect(int position) {
        return false;
      }

      @Override public boolean onSuggestionClick(int position) {
        Cursor item = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
        givenStoreName = item.getString(1);
        searchView.setQuery(givenStoreName, false);
        return true;
      }
    });

    searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
      @Override public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
          StoreAutoCompleteWebSocket.disconnect();
        }
      }
    });

    searchView.setOnSearchClickListener(
        v -> new StoreAutoCompleteWebSocket().connect(STORE_WEBSOCKET_PORT));
  }

  private void getStore(String storeName) {
    GetStoreMetaRequest getStoreMetaRequest = buildRequest(storeName);

    executeRequest(getStoreMetaRequest);
  }

  private void showLoadingDialog() {

    if (loadingDialog == null) {
      loadingDialog = GenericDialogs.createGenericPleaseWaitDialog(getActivity());
    }

    loadingDialog.show();
  }

  private void topStoresAction() {
    navigationManager.navigateTo(V8Engine.getFragmentProvider().newFragmentTopStores());
    if (isAdded()) {
      dismiss();
    }
  }

  private GetStoreMetaRequest buildRequest(String storeName) {
    return GetStoreMetaRequest.of(StoreUtils.getStoreCredentials(storeName),
        AptoideAccountManager.getAccessToken(), aptoideClientUUID.getUniqueIdentifier());
  }

  private void executeRequest(GetStoreMetaRequest getStoreMetaRequest) {
    final IdsRepositoryImpl clientUuid =
        new IdsRepositoryImpl(SecurePreferencesImplementation.getInstance(), getContext());

    new StoreUtilsProxy(clientUuid).subscribeStore(getStoreMetaRequest, getStoreMeta1 -> {
      ShowMessage.asSnack(getView(),
          AptoideUtils.StringU.getFormattedString(R.string.store_followed, storeName));

      dismissLoadingDialog();
      dismiss();
    }, e -> {
      if (e instanceof AptoideWsV7Exception) {
        BaseV7Response baseResponse = ((AptoideWsV7Exception) e).getBaseResponse();

        BaseV7Response.Error error = baseResponse.getError();
        if (StoreUtils.PRIVATE_STORE_ERROR.equals(error.getCode())) {
          DialogFragment dialogFragment = PrivateStoreDialog.newInstance(AddStoreDialog
              .this, PRIVATE_STORE_REQUEST_CODE, storeName, false);
          dialogFragment.show(getFragmentManager(), PrivateStoreDialog.class.getName());
        } else {
          ShowMessage.asSnack(getActivity(), error.getDescription());
        }
        dismissLoadingDialog();
      } else {
        dismissLoadingDialog();
        ShowMessage.asSnack(getActivity(), R.string.error_occured);
      }
    }, storeName);
  }

  void dismissLoadingDialog() {
    loadingDialog.dismiss();
  }

  @Override public void onDetach() {
    super.onDetach();
    mSubscriptions.clear();
    if (storeAutoCompleteWebSocket != null) {
      storeAutoCompleteWebSocket.disconnect();
    }
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mSubscriptions = new CompositeSubscription();
    if (savedInstanceState != null) {
      storeName = savedInstanceState.getString(BundleArgs.STORE_NAME.name());
    }
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(BundleArgs.STORE_NAME.name(), storeName);
  }

  private enum BundleArgs {
    STORE_NAME,
  }
}
