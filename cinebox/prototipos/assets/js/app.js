// Apenas comportamento de navegação. Todos os CRUDs funcionam sem JavaScript.
document.addEventListener('DOMContentLoaded', () => {
  const toggle = document.querySelector('.menu-toggle');
  const nav = document.getElementById('navegacao');
  if (toggle && nav) {
    toggle.addEventListener('click', () => {
      const aberto = toggle.getAttribute('aria-expanded') !== 'true';
      toggle.setAttribute('aria-expanded', String(aberto));
      nav.classList.toggle('is-open', aberto);
    });
  }
});
