package de.ctoffer.assistance.context;

import java.util.function.Consumer;
import java.util.function.Function;

public interface SubSpaceContext {
    void openSpace(SubSpace subSpace);
    void closeSpace(SubSpace subSpace);

    public static SubSpaceContext build(final Consumer<SubSpace> openSpace,
                                        final Consumer<SubSpace> closeSpace) {
        return new SubSpaceContext() {
            private boolean open;

            @Override
            public void openSpace(SubSpace subSpace) {
                if(open) {
                    throw new IllegalStateException("A subspace is already opened!");
                }
                open = true;
                openSpace.accept(subSpace);
            }

            @Override
            public void closeSpace(SubSpace subSpace) {
                if(!open) {
                    throw new IllegalStateException("No subspace is opened - so it can't be closed!");
                }
                open = false;
                closeSpace.accept(subSpace);
            }
        };
    }
}
