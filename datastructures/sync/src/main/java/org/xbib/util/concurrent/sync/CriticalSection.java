package org.xbib.util.concurrent.sync;

class CriticalSection {
    protected boolean locked;
    protected int nBlocked;

    public synchronized void enter() throws InterruptedException {
        if (locked || nBlocked != 0) {
            do {
                try {
                    nBlocked += 1;
                    wait();
                    nBlocked -= 1;
                } catch (InterruptedException x) {
                    nBlocked -= 1;
                    notify();
                    throw x;
                }
            } while (locked);
        }
        locked = true;
    }

    public synchronized void leave() {
        locked = false;
        if (nBlocked != 0) {
            notify();
        }
    }
} 

