/* 이음인사이트 공통 스크립트 */
(function () {
  'use strict';

  /* ---------------------------------------------------------
     1. 모바일 메뉴
     --------------------------------------------------------- */
  function initMobileMenu() {
    var toggle = document.getElementById('menu-toggle');
    var menu = document.getElementById('mobile-menu');
    if (!toggle || !menu) return;
    var icon = toggle.querySelector('.material-symbols-outlined');

    function setOpen(open) {
      menu.classList.toggle('hidden', !open);
      toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
      toggle.setAttribute('aria-label', open ? '메뉴 닫기' : '메뉴 열기');
      if (icon) icon.textContent = open ? 'close' : 'menu';
    }

    toggle.addEventListener('click', function () {
      setOpen(menu.classList.contains('hidden'));
    });
    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape') setOpen(false);
    });
    // 데스크톱 폭으로 넓어지면 자동으로 닫기
    window.addEventListener('resize', function () {
      if (window.innerWidth >= 1024) setOpen(false);
    });
  }

  /* ---------------------------------------------------------
     2. KR / EN (구글 번역 위젯 + googtrans 쿠키)
        - EN 선택: googtrans=/ko/en 쿠키 저장 후 새로고침 → 위젯이 자동 번역
        - KR 선택: 쿠키 삭제 후 새로고침 → 원문
        - 영어 상태일 때만 구글 번역 스크립트를 불러와 평소 로딩 속도에 영향 없음
     --------------------------------------------------------- */
  function getTransLang() {
    var m = document.cookie.match(/(?:^|;\s*)googtrans=([^;]*)/);
    if (!m) return 'ko';
    var parts = decodeURIComponent(m[1]).split('/');
    return parts[2] || 'ko';
  }

  function clearTransCookie() {
    var expire = 'expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/';
    var host = location.hostname;
    document.cookie = 'googtrans=; ' + expire;
    document.cookie = 'googtrans=; ' + expire + '; domain=' + host;
    document.cookie = 'googtrans=; ' + expire + '; domain=.' + host;
    // www.example.com 이면 .example.com 에 저장된 쿠키도 삭제
    var bare = host.replace(/^www\./, '');
    if (bare !== host) document.cookie = 'googtrans=; ' + expire + '; domain=.' + bare;
  }

  function setLanguage(lang) {
    if (lang === getTransLang()) return;
    if (lang === 'ko') {
      clearTransCookie();
    } else {
      document.cookie = 'googtrans=/ko/' + lang + '; path=/';
    }
    location.reload();
  }

  function loadGoogleTranslate() {
    window.googleTranslateElementInit = function () {
      /* global google */
      new google.translate.TranslateElement({
        pageLanguage: 'ko',
        includedLanguages: 'ko,en',
        autoDisplay: false
      }, 'google_translate_element');
    };
    var s = document.createElement('script');
    s.src = 'https://translate.google.com/translate_a/element.js?cb=googleTranslateElementInit';
    s.async = true;
    document.body.appendChild(s);
  }

  /* ---------------------------------------------------------
     2-1. 영문 고정 문구 (구글 번역 대신 지정한 영문 사용)
        - data-en="About us"        : EN일 때 글자를 그대로 교체
        - data-en-html="<b>..</b>"  : EN일 때 HTML로 교체 (굵게 등)
        - data-ko-only              : EN일 때 숨김
        교체한 요소는 notranslate 처리해 구글 번역이 다시 바꾸지 않게 함
     --------------------------------------------------------- */
  function applyFixedEnglish() {
    document.querySelectorAll('[data-en], [data-en-html]').forEach(function (el) {
      var html = el.getAttribute('data-en-html');
      var text = el.getAttribute('data-en');
      if (html) {
        el.innerHTML = html;
      } else if (text) {
        el.textContent = text;
      } else {
        return; // 영문 이름이 비어 있으면 구글 번역에 맡김
      }
      el.classList.add('notranslate');
      el.setAttribute('translate', 'no');
    });
    document.querySelectorAll('[data-ko-only]').forEach(function (el) {
      el.hidden = true;
      el.style.display = 'none';
    });
  }

  /* ---------------------------------------------------------
     2-2. 영문 용어 고정 (구글 번역 결과를 한 번 더 보정)
        본문처럼 data-en을 달기 어려운 곳의 잘못된 번역을 바로잡음
     --------------------------------------------------------- */
  var EN_GLOSSARY = [
    [/\bE+eum\b/g, 'Eum'],
    [/\bIeum\b/g, 'Eum'],
    [/\bBaek Ji[- ]?[Hh]oon\b/g, 'Ji Hwoon Baek'],
    [/\bJi[- ]?[Hh]oon Baek\b/g, 'Ji Hwoon Baek'],
    [/\bBusinessand\b/g, 'Business and'],
    [/\bOverseas\b/g, 'Global'],
    [/\boverseas\b/g, 'global'],
    [/\bDomestic\b/g, 'Korean'],
    [/\bdomestic\b/g, 'Korean']
  ];

  function fixTextNode(node) {
    var text = node.nodeValue;
    if (!text || !/[A-Za-z]/.test(text)) return;
    var fixed = text;
    EN_GLOSSARY.forEach(function (rule) { fixed = fixed.replace(rule[0], rule[1]); });
    if (fixed !== text) node.nodeValue = fixed;
  }

  function fixTree(root) {
    if (root.nodeType === 3) { fixTextNode(root); return; }
    if (root.nodeType !== 1) return;
    var walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, null);
    var n;
    while ((n = walker.nextNode())) fixTextNode(n);
  }

  function watchEnglishGlossary() {
    var pending = [];
    var timer = null;
    var observer = new MutationObserver(function (records) {
      records.forEach(function (r) {
        if (r.type === 'characterData') pending.push(r.target);
        r.addedNodes.forEach(function (n) { pending.push(n); });
      });
      if (timer) return;
      timer = setTimeout(function () {
        timer = null;
        var list = pending; pending = [];
        list.forEach(fixTree);
      }, 50);
    });
    observer.observe(document.body, { childList: true, subtree: true, characterData: true });
    fixTree(document.body);
  }

  function initLanguage() {
    var current = getTransLang();
    document.querySelectorAll('.lang-btn').forEach(function (btn) {
      btn.classList.toggle('is-active', btn.dataset.lang === current);
      btn.setAttribute('aria-pressed', btn.dataset.lang === current ? 'true' : 'false');
      btn.addEventListener('click', function () {
        setLanguage(btn.dataset.lang);
      });
    });
    if (current !== 'ko') {
      document.documentElement.lang = current;
      if (current === 'en') {
        applyFixedEnglish();
        watchEnglishGlossary();
      }
      loadGoogleTranslate();
    }
  }

  /* ---------------------------------------------------------
     3. 탭 (해외 인허가 국가별 트랙)
        <div data-tabs>
          <button data-tab-target="us">  ...  <div data-tab-panel="us">
     --------------------------------------------------------- */
  function initTabs() {
    document.querySelectorAll('[data-tabs]').forEach(function (root) {
      var buttons = root.querySelectorAll('[data-tab-target]');
      var panels = root.querySelectorAll('[data-tab-panel]');

      function show(key, focus) {
        var found = false;
        buttons.forEach(function (b) {
          var on = b.dataset.tabTarget === key;
          if (on) found = true;
          b.classList.toggle('is-active', on);
          b.setAttribute('aria-selected', on ? 'true' : 'false');
        });
        if (!found) return false;
        panels.forEach(function (p) {
          p.hidden = p.dataset.tabPanel !== key;
        });
        if (focus) {
          var panel = root.querySelector('[data-tab-panel="' + key + '"]');
          if (panel && window.innerWidth < 768) {
            panel.scrollIntoView({ behavior: 'smooth', block: 'start' });
          }
        }
        return true;
      }

      buttons.forEach(function (b) {
        b.addEventListener('click', function () {
          show(b.dataset.tabTarget, true);
          if (history.replaceState) history.replaceState(null, '', '#' + b.dataset.tabTarget);
        });
      });

      var hash = location.hash.replace('#', '');
      if (!hash || !show(hash, false)) {
        show(buttons[0].dataset.tabTarget, false);
      }
      // 같은 페이지에서 /global#cn 처럼 해시만 바뀌는 경우
      window.addEventListener('hashchange', function () {
        show(location.hash.replace('#', ''), true);
      });
    });
  }

  /* ---------------------------------------------------------
     4. 문의하기 폼
     --------------------------------------------------------- */
  function initInquiryForm() {
    var form = document.getElementById('inquiry-form');
    if (!form) return;

    // ?type=GLOBAL 처럼 넘어오면 문의 유형 자동 선택
    var type = new URLSearchParams(location.search).get('type');
    if (type && form.inquiryType) {
      var opt = form.inquiryType.querySelector('option[value="' + type + '"]');
      if (opt) form.inquiryType.value = type;
    }

    var toggleBtn = document.getElementById('privacy-toggle');
    var privacyBox = document.getElementById('privacy-box');
    if (toggleBtn && privacyBox) {
      toggleBtn.addEventListener('click', function () {
        privacyBox.hidden = !privacyBox.hidden;
        toggleBtn.setAttribute('aria-expanded', privacyBox.hidden ? 'false' : 'true');
      });
    }

    var errorBox = document.getElementById('form-error');
    var submitBtn = form.querySelector('button[type="submit"]');
    var submitLabel = submitBtn ? submitBtn.innerHTML : '';

    function showError(msg) {
      errorBox.textContent = msg;
      errorBox.hidden = false;
    }

    form.addEventListener('submit', function (e) {
      e.preventDefault();
      errorBox.hidden = true;

      var required = form.querySelectorAll('[required]');
      var firstInvalid = null;
      required.forEach(function (el) {
        var ok = el.type === 'checkbox' ? el.checked : el.value.trim() !== '';
        if (ok && el.type === 'email') ok = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(el.value.trim());
        el.classList.toggle('is-invalid', !ok);
        if (!ok && !firstInvalid) firstInvalid = el;
      });
      if (firstInvalid) {
        showError(firstInvalid.type === 'checkbox'
          ? '개인정보 수집·이용 및 기밀유지 기준에 동의해 주세요.'
          : '필수 항목(*)을 정확히 입력해 주세요.');
        firstInvalid.focus();
        return;
      }

      var payload = {
        inquiryType: form.inquiryType.value,
        companyName: form.companyName.value.trim(),
        contactName: form.contactName.value.trim(),
        email: form.email.value.trim(),
        phone: form.phone.value.trim(),
        productTarget: form.productTarget.value.trim(),
        content: form.content.value.trim(),
        agreed: form.agreed.checked,
        website: form.website.value
      };

      submitBtn.disabled = true;
      submitBtn.innerHTML = '접수 중...';

      fetch('/api/inquiry', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
        .then(function (res) {
          return res.json().catch(function () { return {}; }).then(function (data) {
            if (!res.ok) throw new Error(data.message || '접수 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.');
            return data;
          });
        })
        .then(function () {
          form.hidden = true;
          var done = document.getElementById('form-done');
          done.hidden = false;
          done.scrollIntoView({ behavior: 'smooth', block: 'center' });
        })
        .catch(function (err) {
          showError(err.message);
          submitBtn.disabled = false;
          submitBtn.innerHTML = submitLabel;
        });
    });

    form.querySelectorAll('[required]').forEach(function (el) {
      el.addEventListener('input', function () { el.classList.remove('is-invalid'); });
      el.addEventListener('change', function () { el.classList.remove('is-invalid'); });
    });
  }

  document.addEventListener('DOMContentLoaded', function () {
    initMobileMenu();
    initLanguage();
    initTabs();
    initInquiryForm();
  });
})();