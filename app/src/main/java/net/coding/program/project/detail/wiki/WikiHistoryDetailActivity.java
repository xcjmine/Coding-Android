package net.coding.program.project.detail.wiki;

import android.support.annotation.NonNull;
import android.webkit.WebView;

import com.orhanobut.logger.Logger;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.wiki.Wiki;
import net.coding.program.network.model.wiki.WikiHistory;
import net.coding.program.project.detail.ProjectActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_wiki_history_detail)
public class WikiHistoryDetailActivity extends BackActivity {

    @Extra
    ProjectActivity.ProjectJumpParam project;

    @Extra
    Wiki wiki;

    @Extra
    int version;

    Wiki wikiDetail;

    @ViewById
    WebView webView;

    @AfterViews
    void initWikiHistoryDetailActivity() {
        onRefresh();
    }

    private void onRefresh() {
        Network.getRetrofit(this)
                .getWikiDetail(project.mUser, project.mProject, wiki.iid, wiki.lastVersion)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Wiki>(this) {
                    @Override
                    public void onSuccess(Wiki data) {
                        super.onSuccess(data);

                        wikiDetail = data;
                        displayWebviewContent(data.html);
                        Logger.d(data.html);
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
//                        BlankViewDisplay.setBlank(0, WikiMainActivity.this, false, blankLayout, v -> onRefrush());
                    }
                });
    }

    void displayWebviewContent(String html) {
        Global.setWebViewContent(webView, "markdown.html", html);
    }

    @Click(R.id.rollbackLayout)
    void onClickRollback() {
        String msg = "请问是否要恢复到当前版本？";
        showDialog("", msg, (dialog, which) -> rollbackWiki());
    }

    private void rollbackWiki() {
        Network.getRetrofit(this)
                .rollbackWiki(project.mUser, project.mProject, wiki.iid, version)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<WikiHistory>(this) {
                    @Override
                    public void onSuccess(WikiHistory data) {
                        super.onSuccess(data);
                        showProgressBar(false);
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        showProgressBar(false);
                    }
                });

        showProgressBar(true);
    }

}
