var render = function (theme, data, meta, require) {
    theme('2-column-left', {
        title: data.title,
        header: [
            {
                partial: 'header',
                context: {hideNavBar: true}
            }
        ],
        leftColumn: [
            {
                partial: 'left-column',
                context: {hideTags: true}
            }
        ],
        search: [
            {
                partial: 'search',
                context: {hideSearch: true}
            }
        ],
        pageHeader: [
            {
                partial: 'page-header',
                context: {}
            }
        ],
        pageContent: [
            {
                partial: 'page-content-public-stores',
                context: {currentPage: data.currentPageNumber, stores: data.stores}
            }
        ]
    });
};