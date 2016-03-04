var render = function (theme, data, meta, require) {
    theme('1-column', {
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
                context: {}
            }
        ],
        search: [
            {
                partial: 'search',
                context: {}
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