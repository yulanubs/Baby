package com.ozj.baby.mvp.model.rx;

import android.content.Context;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.orhanobut.logger.Logger;
import com.ozj.baby.di.scope.ContextLife;
import com.ozj.baby.mvp.model.dao.SouvenirDao;
import com.ozj.baby.mvp.model.dao.UserDao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/4/20.
 */
public class RxLeanCloud {
    private static volatile RxLeanCloud mRxLeanCloud;
    private Context mContext;

    @Singleton
    @Inject
    public RxLeanCloud(@ContextLife("Application") Context context) {
        mContext = context;
    }


    public RxLeanCloud getInstance(Context context) {
        if (mRxLeanCloud == null) {
            synchronized (RxLeanCloud.class) {
                if (mRxLeanCloud == null) {
                    mRxLeanCloud = new RxLeanCloud(context);

                }
            }
        }
        return mRxLeanCloud;
    }

    public Observable<Boolean> SaveByLeanCloud(final AVObject object) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            subscriber.onNext(true);
                        } else {
                            subscriber.onNext(false);
                            subscriber.onError(e);
                        }
                        subscriber.onCompleted();
                    }
                });
            }
        });
    }

    public Observable<Boolean> SaveUserByLeanCloud(final AVUser user) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            subscriber.onNext(true);
                        } else {
                            subscriber.onError(e);
                            Logger.e(e.getMessage());
                        }
                        subscriber.onCompleted();
                    }
                });

            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

    }

    public Observable<AVUser> GetUserByLeanCloud(final String objectId) {

        return Observable.create(new Observable.OnSubscribe<AVUser>() {
            @Override
            public void call(Subscriber<? super AVUser> subscriber) {

                AVQuery<AVUser> query = AVUser.getQuery();

                try {
                    AVUser user = query.include(UserDao.AVATARURL).get(objectId);
                    subscriber.onNext(user);

                } catch (AVException e) {
                    e.printStackTrace();
                    subscriber.onError(e);

                }
                subscriber.onCompleted();


            }
        }).subscribeOn(Schedulers.io());

    }

    public Observable<AVUser> GetUserByUsername(final String username) {
        return Observable.create(new Observable.OnSubscribe<AVUser>() {
            @Override
            public void call(Subscriber<? super AVUser> subscriber) {
                AVQuery<AVUser> query = AVUser.getQuery();
                try {
                    AVUser user = query.whereEqualTo(UserDao.USERNAME, username).getFirst();
                    subscriber.onNext(user);
                } catch (AVException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
                subscriber.onCompleted();

            }
        }).subscribeOn(Schedulers.io());

    }

    public Observable<List<AVObject>> GetALlSouvenirByLeanCloud(final String authorId, final String theOtherID, final int size, final int page) {

        return Observable.create(new Observable.OnSubscribe<List<AVObject>>() {
            @Override
            public void call(Subscriber<? super List<AVObject>> subscriber) {

                AVQuery<AVObject> query = AVQuery.getQuery(SouvenirDao.TABLENAME);
                query.whereEqualTo(SouvenirDao.SOUVENIR_AUTHORID, authorId);
                AVQuery<AVObject> query1 = AVQuery.getQuery(SouvenirDao.TABLENAME);
                query1.whereEqualTo(SouvenirDao.SOUVENIR_AUTHORID, theOtherID);
                List<AVQuery<AVObject>> queries = new ArrayList<>();
                queries.add(query);
                queries.add(query1);
                AVQuery<AVObject> mainQuery = AVQuery.or(queries);
                mainQuery.setLimit(size);
                mainQuery.setSkip(size * page);
                try {
                    List<AVObject> list = mainQuery.find();
                    subscriber.onNext(list);
                } catch (AVException e) {
                    e.printStackTrace();
                    subscriber.onError(e);

                }
                subscriber.onCompleted();

            }
        }).subscribeOn(Schedulers.io());

    }

//    public Observable<Integer> UploadAvatar(final AVFile file, final AVUser user) {
//        return Observable.create(new Observable.OnSubscribe<Integer>() {
//            @Override
//            public void call(final Subscriber<? super Integer> subscriber) {
//                file.saveInBackground(new SaveCallback() {
//                    @Override
//                    public void done(AVException e) {
//                        if (e == null) {
//                            Logger.d("上传头像成功");
//                        } else {
//                            subscriber.onError(e);
//                        }
//                    }
//                }, new ProgressCallback() {
//                    @Override
//                    public void done(Integer integer) {
//                        subscriber.onNext(integer);
//                    }
//                });
//                user.put(UserDao.AVATARURL, file);
//                user.saveInBackground(new SaveCallback() {
//                    @Override
//                    public void done(AVException e) {
//                        if (e == null) {
//                            subscriber.onCompleted();
//                        } else {
//                            subscriber.onError(e);
//                        }
//                    }
//                });
//            }
//        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
//    }

    public Observable<String> UploadPicture(final AVFile avFile) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                avFile.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            subscriber.onNext(avFile.getUrl());
                            subscriber.onCompleted();
                        } else {
                            subscriber.onError(e);
                        }
                    }
                });
            }
        }).subscribeOn(Schedulers.io());

    }

}
