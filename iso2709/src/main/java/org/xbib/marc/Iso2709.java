package org.xbib.marc;

import org.xbib.io.field.MarcField;
import rx.Observable;

public class Iso2709 extends Observable<MarcField> {
    /**
     * Creates an Observable with a Function to execute when it is subscribed to.
     * <p>
     * <em>Note:</em> Use {@link #create(OnSubscribe)} to create an Observable, instead of this constructor,
     * unless you specifically have a need for inheritance.
     *
     * @param f {@link OnSubscribe} to be executed when {@link #subscribe(Subscriber)} is called
     */
    protected Iso2709(OnSubscribe<MarcField> f) {
        super(f);
    }


}
