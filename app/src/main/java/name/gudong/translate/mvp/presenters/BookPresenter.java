/*
 *  Copyright (C) 2015 GuDong <gudong.name@gmail.com>
 *
 *  This file is part of GdTranslate
 *
 *  GdTranslate is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  GdTranslate is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GdTranslate.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package name.gudong.translate.mvp.presenters;

import android.content.Context;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.orhanobut.logger.Logger;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import name.gudong.translate.mvp.model.SingleRequestService;
import name.gudong.translate.mvp.model.WarpAipService;
import name.gudong.translate.mvp.model.entity.translate.Result;
import name.gudong.translate.mvp.views.IBookView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by GuDong on 2/28/16 17:02.
 * Contact with gudong.name@gmail.com.
 */
public class BookPresenter extends BasePresenter<IBookView> {
    @Inject
    public BookPresenter(LiteOrm liteOrm, WarpAipService apiService, SingleRequestService singleRequestService, Context context) {
        super(liteOrm, apiService, singleRequestService,context);
    }

    public void getWords() {
        getAllWordsWarpByObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Result>>() {
                    @Override
                    public void call(List<Result> transResultEntities) {
                        mView.fillData(transResultEntities);
                    }
                });
    }

    public void deleteWords(Result entity){
        deleteWordsByObservable(entity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((integer -> {
                    if (integer > 0) {
                        mView.deleteWordSuccess(entity);
                    } else {
                        mView.deleteWordFail();
                    }
                }), throwable -> {
                    mView.onError(throwable);
                });
    }

    private Observable<List<Result>> getAllWordsWarpByObservable() {
        return makeObservable(getAllWordsReal());
    }

    private Observable<Integer> deleteWordsByObservable(Result entity) {
        return makeObservable(deleteWordReal(entity));
    }

    // callable 对象可以作为线程体执行
    private Callable<List<Result>> getAllWordsReal() {
        return new Callable<List<Result>>() {
            @Override
            public List<Result> call() throws Exception {
                QueryBuilder<Result> qb = new QueryBuilder<>(Result.class)
                        .appendOrderDescBy(Result.COL_ID);

                List<Result> results = mLiteOrm.query(qb);
                Logger.i(" results.size() "+results.size());
                return results;
            }
        };
    }

    private Callable<Integer> deleteWordReal(Result entity) {
        return () -> mLiteOrm.delete(entity);
    }

}
