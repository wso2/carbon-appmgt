$(function () {
    History.Adapter.bind(window, 'statechange', function () {

        var state = History.getState();
        if (state.data.id === 'sort-assets') {
            renderAssets(state.data.context);
        } else if (state.data.id === 'top-assets') {
            var el = $('.store-left'), data = state.data.context;
            async.parallel({
                               topAssets: function (callback) {
                                   caramel.render('top-assets', data.body['top-assets'].context, callback);
                               }
                           }, function (err, result) {
                theme.loaded(el, result.sort);
                el.html(result.topAssets);
                $("#top-asset-slideshow-gadget").carouFredSel({
                                                                  items: 4,
                                                                  width: "100%",
                                                                  infinite: false,
                                                                  auto: false,
                                                                  circular: false,
                                                                  pagination: "#top-asset-slideshow-pag-gadget"

                                                              });

                $("#top-asset-slideshow-site").carouFredSel({
                                                                items: 4,
                                                                width: "100%",
                                                                infinite: false,
                                                                auto: false,
                                                                circular: false,
                                                                pagination: "#top-asset-slideshow-pag-site"

                                                            });

                $("#top-asset-slideshow-ebook").carouFredSel({
                                                                 items: 4,
                                                                 width: "100%",
                                                                 infinite: false,
                                                                 auto: false,
                                                                 circular: false,
                                                                 pagination: "#top-asset-slideshow-pag-ebook"

                                                             });
                mouseStop();
                $(document).scrollTop(0);
            });
        }
    });

    var buildParams = function (query) {
        return 'query=' + query;
    };

    var search = function () {
        var url, searchVal = $('#search').val();

        currentPage = 1;
        if (store.asset) {
            url = caramel.tenantedUrl('/assets/' + store.asset.type + '/?' + buildParams(searchVal));
            caramel.data({
                             title: null,
                             header: ['header'],
                             body: ['assets', 'sort-assets']
                         }, {
                             url: url,
                             success: function (data, status, xhr) {
                                 //TODO: Integrate a new History.js library to fix this

                                 if ($.browser.msie == true && $.browser.version < 10) {
                                     renderAssets(data);
                                 } else {
                                     History.pushState({
                                                           id: 'sort-assets',
                                                           context: data
                                                       }, document.title, url);
                                 }
                             },
                             error: function (xhr, status, error) {
                                 theme.loaded($('#assets-container').parent(), '<p>Error while retrieving data.</p>');
                             }
                         });
            theme.loading($('#assets-container').parent());
        } else if (searchVal.length > 0 && searchVal != undefined) {
            url = caramel.tenantedUrl('/?' + buildParams(searchVal));
            window.location = url;
        }

        $('.search-bar h2').find('.page').text(' / Search: "' + searchVal + '"');
    };

    $('#search-dropdown-cont').ontoggle = function ($, divobj, state) {
        var icon = $('#search-dropdown-arrow').find('i'), cls = icon.attr('class');
        icon.removeClass().addClass(cls == 'icon-sort-down' ? 'icon-sort-up' : 'icon-sort-down');
    }

    $('#search').keypress(function (e) {
        if (e.keyCode === 13) {
            if ($('#search-dropdown-cont').is(':visible')) {
                $('#search').val('');
                makeQuery();
            }
            search();
        } else if (e.keyCode == 27) {
            $('#search-dropdown-cont').toggle();
        }

    })
        .click(function (e) {
                   $(this).animate({width: '500px'}, 100);
                   e.stopPropagation();
               });

    $(document).click(function () {
        $('#search').animate({width: '100%'});
    });

    $('#search-button').click(function () {
        if ($('#search').val() == '') return;
        if ($('#search-dropdown-cont').is(':visible')) {
            $('#search').val('');
            makeQuery();
        }
        search();
    });

    $('#search-dropdown-arrow').click(function (e) {
        e.stopPropagation();
        e.preventDefault();
        var icon = $(this).find('i'), cls = icon.attr('class');
        icon.removeClass().addClass(cls == 'icon-sort-down' ? 'icon-sort-up' : 'icon-sort-down');
        if ($('#search').val().length > 0) {
            if ($('#search').val().indexOf(',')) {
                var qarray = $('#search').val().split(",");
                if (qarray.length > 0) {
                    $('#search-dropdown-cont').children('div').each(function () {
                        var $this = $(this);
                        $this.find('input').val('')
                    });
                    for (var i = 0; i < qarray.length; i++) {
                        $('#search-dropdown-cont').children('div').each(function () {
                            var $this = $(this);
                            var idVal = $this.find('input').attr('id').toLowerCase();
                            if (idVal == qarray[i].split(':')[0].toLowerCase()) {
                                $this.find('input').val(qarray[i].split(':')[1])
                            }
                        });
                    }
                }
            }
        }
        $('#search-dropdown-cont').delay(300).slideToggle("fast");
        $('#search').trigger('click');
    });

    $('#searchBy li').click(function(e){
        $('#searchSelect').val($(this).data('value'));
    });

    $('#searchTxt').keypress(function (e) {
        if (e.keyCode == 13) {  // detect the enter key
            var searchTerm = $(this).val();
            e.stopPropagation();
            e.preventDefault();
            searchAsset(searchTerm);
        }
    });

    $('#searchBtn').click(function(e){
        var searchTerm = $('#searchTxt').val();
        e.stopPropagation();
        e.preventDefault();
        searchAsset(searchTerm);
    });

    function searchAsset(searchTerm) {
        if (checkNonSpecial(searchTerm)) {
            var searchSelect = $('#searchSelect').val();
            if (searchSelect !== "App") {
                searchTerm = searchSelect + ":" + "\"" + searchTerm + "\"";
            }
            var searchUrl = $('#searchUrl').val();
            if (searchUrl.indexOf('type=webapp') > -1) {
                location.href =
                location.protocol + '//' + location.host + searchUrl + '&query=' + searchTerm;
            } else if (searchUrl.indexOf('type=site') > -1) {
                location.href =
                location.protocol + '//' + location.host + searchUrl + '&query=' + searchTerm;
            } else {
                location.href =
                location.protocol + '//' + location.host + searchUrl + '?query=' + searchTerm;
            }
        }

    }
    function checkNonSpecial(value){
        var non_special_regex = /^[A-Za-z][A-Za-z0-9\s-]*$/;
        return non_special_regex.test(value);
    }

    $('#search-dropdown-close').click(function (e) {
        e.stopPropagation();
        $('#search-dropdown-cont').toggle();
        var icon = $('#search-dropdown-arrow').find('i'), cls = icon.attr('class');
        icon.removeClass().addClass(cls == 'icon-sort-down' ? 'icon-sort-up' : 'icon-sort-down');
    });

    $('html').click(function () {
        if ($('#search-dropdown-cont').is(':visible')) {
            $('#search-dropdown-cont').hide();
            var icon = $('#search-dropdown-arrow').find('i'), cls = icon.attr('class');
            icon.removeClass().addClass(cls == 'icon-sort-down' ? 'icon-sort-up' : 'icon-sort-down');
        }

    });

    $('#search-dropdown-cont').click(function (e) {
        e.stopPropagation();
    });

    $('#search-dropdown-cont').find('input').keypress(function (e) {
        if (e.keyCode == 13) {
            $('#search-button2').trigger('click');
        }
    });

    var makeQuery = function () {

        $('#search-dropdown-cont').children('div').each(function () {
            var $this = $(this);
            if ($('#search').val().length > 0) {
                if ($this.find('input').val().length > 0) {
                    $('#search').val($('#search').val() + ' ' + $this.find('input').attr('name') + ':"' + $this.find('input').val() + '"');
                }
            } else {
                if ($this.find('input').val().length > 0) {
                    $('#search').val($this.find('input').attr('name') + ':"' + $this.find('input').val() + '"');
                }
            }
        });
    }

    $('#search-button2').click(function () {
        $('#search').val('');

        makeQuery();
        if ($('#search').val() == '') return;
        search();
        $('#search-dropdown-cont input').val('');
        return false;
    });
});