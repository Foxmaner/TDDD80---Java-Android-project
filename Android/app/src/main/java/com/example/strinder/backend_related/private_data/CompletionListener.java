package com.example.strinder.backend_related.private_data;

/** This is used in combinations with the GoogleServices class. This should be implemented by
 * classes that are interested in when the code that is sent into requestPrivateData is done
 * running.
 */
@FunctionalInterface
public interface CompletionListener {
    /** Is run after the last snippet of code that is sent into the requestPrivateData method.
     * @see GoogleServices
     */
    void onCompletion();
}
