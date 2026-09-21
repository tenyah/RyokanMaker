function carouselMove(btn, direction) {
  var thumb = btn.closest('[data-carousel]');
  var slide = thumb.querySelector('.slide');
  var images = slide.querySelectorAll('img');
  var total = images.length;
  if (total === 0) return;

  var current = parseInt(thumb.dataset.index || '0', 10);
  var next = (current + direction + total) % total;
  thumb.dataset.index = next;

  slide.style.transform = 'translateX(-' + (next * 100) + '%)';

  var dots = thumb.querySelectorAll('.dots span');
  dots.forEach(function (dot, i) {
    dot.classList.toggle('active', i === next);
  });
}
