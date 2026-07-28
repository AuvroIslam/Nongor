package org.nongor.app.actions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The action URI comes from the model, so only real web links may ever reach ACTION_VIEW. */
class AppActionLauncherTest {

    @Test fun acceptsHttpAndHttps() {
        assertEquals("https://who.int/floods", AppActionLauncher.safeWebUrl("https://who.int/floods"))
        assertEquals("http://example.com", AppActionLauncher.safeWebUrl("http://example.com"))
        assertEquals("https://example.com/a?b=1", AppActionLauncher.safeWebUrl(" https://example.com/a?b=1 "))
    }

    @Test fun rejectsNonWebSchemes() {
        // Each of these could exfiltrate data or trigger an unintended app if handed to ACTION_VIEW.
        assertNull(AppActionLauncher.safeWebUrl("javascript:alert(1)"))
        assertNull(AppActionLauncher.safeWebUrl("file:///data/data/org.nongor.app/secret"))
        assertNull(AppActionLauncher.safeWebUrl("content://com.android.contacts/data"))
        assertNull(AppActionLauncher.safeWebUrl("intent://scan/#Intent;scheme=zxing;end"))
        assertNull(AppActionLauncher.safeWebUrl("tel:+880123"))
        assertNull(AppActionLauncher.safeWebUrl("ftp://files.example.com"))
    }

    @Test fun rejectsMalformedOrSchemeless() {
        assertNull(AppActionLauncher.safeWebUrl(""))
        assertNull(AppActionLauncher.safeWebUrl("not a url"))
        assertNull(AppActionLauncher.safeWebUrl("://noscheme"))
        assertNull(AppActionLauncher.safeWebUrl("https://"))    // no host
    }
}
