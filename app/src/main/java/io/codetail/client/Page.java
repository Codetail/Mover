package io.codetail.client;

import com.squareup.otto.Bus;

public interface Page{

    /**
     * @return current page category
     */
    public String getCategory();

    public Page from(String source);

    public void postEvent(Bus bus);

    public static interface PaginatedPage extends Page{


        /**
         * @return is current page main page of category
         */
        public boolean isMainPage();

        /**
         * if {@link #hasPagination()} use this method
         * to get pages count
         *
         * @return pagination pages count
         */
        public int getPagesCount();

        /**
         * Some pages can be paginated, this method is
         * provides boolean result, current page have another ones
         *
         * @return true if has another pages
         */
        public boolean hasPagination();

        /**
         * Return current page number
         *
         * @return current fetched page number
         */
        public int getCurrentPageNumber();

    }

}
