package io.codetail.client;

public class State {

    /**
     * Before start loading page, show requesting page
     */
    public static class OnStartLoadingPage{
        public final int page;

        public OnStartLoadingPage(int page) {
            this.page = page;
        }
    }

    /**
     * Abstract layer for response
     */
    public abstract static class OnResponseEvent<T extends Page>{
        public final T page;

        public OnResponseEvent(T page) {
            this.page = page;
        }
    }

    /**
     * Result of pages request
     */
    public static class OnPageResponseEvent extends OnResponseEvent<Page>{

        public OnPageResponseEvent(Page page) {
            super(page);
        }
    }

    /**
     * Result of paginated page
     */
    public static class OnPaginatedPageResponseEvent extends OnResponseEvent<Page.PaginatedPage>{

        public OnPaginatedPageResponseEvent(Page.PaginatedPage page) {
            super(page);
        }
    }

    /**
     * Result of search request
     */
    public static class OnSearchResponseEvent extends OnResponseEvent<Page.PaginatedPage>{

        public OnSearchResponseEvent(Page.PaginatedPage page) {
            super(page);
        }
    }

}
