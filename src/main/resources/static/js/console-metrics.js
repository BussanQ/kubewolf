(function () {
    'use strict';
    layui.use(['jquery'], function () {
        var $ = layui.jquery;
        var reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)');
        var animations = new Map();

        function available(value) {
            return typeof value === 'number' && Number.isFinite(value) && value >= 0;
        }

        function format(value) {
            return available(value) ? Number(value.toFixed(2)).toString() : '--';
        }

        function text(id, value) {
            document.getElementById(id).textContent = value;
        }

        function finishAnimations() {
            animations.forEach(function (animation) {
                cancelAnimationFrame(animation.frame);
                animation.finish();
            });
            animations.clear();
        }

        function animateNumber(id, value, formatter) {
            formatter = formatter || function (number) { return number.toFixed(2); };
            var element = document.getElementById(id);
            var previous = animations.get(id);
            if (previous) cancelAnimationFrame(previous.frame);
            animations.delete(id);
            if (!available(value)) { element.textContent = '--'; return; }
            var finish = function () { element.textContent = formatter(value); };
            if (reducedMotion.matches || document.hidden) { finish(); return; }
            var start = null;
            var animation = {frame: 0, finish: finish};
            function step(time) {
                if (start === null) start = time;
                var progress = Math.min((time - start) / 1200, 1);
                element.textContent = formatter(value * (1 - Math.pow(1 - progress, 3)));
                if (progress < 1) animation.frame = requestAnimationFrame(step);
                else { finish(); animations.delete(id); }
            }
            animations.set(id, animation);
            animation.frame = requestAnimationFrame(step);
        }

        function renderGauge(key, rate) {
            var panel = document.getElementById(key + '-panel');
            var valid = available(rate);
            var fill = valid ? Math.min(rate, 1) * 100 : 0;
            panel.classList.remove('is-loading');
            panel.classList.toggle('has-data', valid);
            panel.classList.toggle('is-unavailable', !valid);
            panel.querySelector('.gauge-progress').style.strokeDashoffset = 100 - fill;
            // A round line cap at zero would falsely suggest nonzero utilization.
            panel.querySelector('.gauge-progress').style.opacity = valid && rate > 0 ? 1 : 0;
            panel.querySelector('.metric-load-fill').style.width = fill + '%';
            panel.querySelector('.metric-status span').textContent = valid ? '数据已同步' : '数据异常';
            document.getElementById(key + '-gauge').setAttribute('aria-label',
                document.getElementById(key + '-title').textContent + '，' + (valid ? (rate * 100).toFixed(2) + '%' : '暂无可用数据'));
            animateNumber(key + '-rate', valid ? rate * 100 : null);
        }

        function renderSummary(id, value, count) {
            var panel = document.getElementById(id + '-summary');
            var valid = available(value);
            panel.classList.remove('is-loading');
            panel.classList.toggle('has-data', valid);
            panel.classList.toggle('is-unavailable', !valid);
            panel.querySelector('.summary-state').textContent = valid ? '已同步' : '数据异常';
            animateNumber(id, value, count ? function (number) { return Math.round(number).toLocaleString('zh-CN'); } : format);
        }

        function render(data) {
            renderSummary('imageCount', data.imageCount, true);
            renderSummary('nodes', data.nodes, true);
            renderSummary('cpu', data.cpu, false);
            renderSummary('memory', data.memory, false);
            renderGauge('cpu', data.cpuRate);
            renderGauge('memory', data.memRate);
            renderGauge('gpu', data.gpuRate);
            text('cpu-detail-used', available(data.cpuRate) && available(data.cpu) ? format(data.cpuRate * data.cpu) + ' 核' : '--');
            text('cpu-detail-total', available(data.cpu) ? format(data.cpu) + ' 核' : '--');
            text('memory-detail-used', available(data.memoryUsed) ? format(data.memoryUsed) + ' GiB' : '--');
            text('memory-detail-total', available(data.memory) ? format(data.memory) + ' GiB' : '--');
            text('gpu-detail-used', available(data.gpuRate) ? (data.gpuRate * 100).toFixed(2) + '%' : '--');
            text('gpu-detail-total', available(data.gpuRate) ? '已同步' : '暂无数据');
        }

        $.getJSON('/api/v1/console').done(function (res) {
            render(res.code === 200 && res.data ? res.data : {});
        }).fail(function () { render({}); });

        function visibilityChanged() {
            document.documentElement.classList.toggle('metrics-paused', document.hidden);
            if (document.hidden) finishAnimations();
        }
        document.addEventListener('visibilitychange', visibilityChanged);
        reducedMotion.addEventListener('change', function () {
            if (reducedMotion.matches) finishAnimations();
        });
        window.addEventListener('pagehide', finishAnimations);
        visibilityChanged();
    });
})();
