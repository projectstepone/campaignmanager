package io.appform.campaignmanager.handlebars;

import com.github.jknack.handlebars.Options;
import com.google.common.base.Objects;

import java.io.IOException;

/**
 *
 */

public class CustomHelpers {
    public CharSequence eqstr(final Object obj1, final Options options) throws IOException {
        Object obj2 = options.param(0);
        return Objects.equal(obj1.toString(), obj2.toString()) ? options.fn() : options.inverse();
    }

    public CharSequence neqstr(final Object obj1, final Options options) throws IOException {
        Object obj2 = options.param(0);
        return !Objects.equal(obj1.toString(), obj2.toString()) ? options.fn() : options.inverse();
    }
}
