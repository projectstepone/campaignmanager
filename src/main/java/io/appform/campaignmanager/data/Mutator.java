package io.appform.campaignmanager.data;

/**
 *
 */
@FunctionalInterface
public interface Mutator<T> {
    void mutate(T value);
}
