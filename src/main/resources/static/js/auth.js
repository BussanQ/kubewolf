(function () {
  function install($) {
    if ($.kubewolfAuthInstalled) return;
    $.kubewolfAuthInstalled = true;
    $.ajaxPrefilter(function (options, original, xhr) {
        var url = new URL(options.url, location.href);
        if (url.origin === location.origin && !/^(GET|HEAD|OPTIONS)$/i.test(options.type || 'GET')) {
            xhr.setRequestHeader($('meta[name="csrf-header"]').attr('content') || 'X-CSRF-TOKEN',
                $('meta[name="csrf-token"]').attr('content') || '');
        }
    });
    $(document).ajaxError(function (event, xhr) {
        if (window.layer) layer.closeAll('loading');
        if (xhr.status === 401 && location.pathname !== '/login') {
            top.location.href = '/login';
            return;
        }
        var message = xhr.responseJSON && xhr.responseJSON.message || '请求失败，请稍后重试';
        if (window.layer) layer.msg(message, {icon: 2});
    });
  }
  install(jQuery);
  layui.use(['jquery'], function () { install(layui.jquery); });
  jQuery(document).on('click', '.kw-logout', function () {
        jQuery.post('/logout').done(function () { top.location.href = '/login'; });
    });
})();
