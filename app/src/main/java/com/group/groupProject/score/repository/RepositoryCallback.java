package com.group.groupProject.score.repository;

public interface RepositoryCallback<T> {
    void onSuccess(T data);

    void onError(String message, Throwable throwable);
}

