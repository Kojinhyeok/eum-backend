/** 이음인사이트 브랜드 팔레트: C. Neon Lime & Navy
 *  #FFFFFF / #F5F9F0 / #E5F5CF / #B7F34D / #263F56
 *
 *  - primary : Navy (메인 색, 제목·버튼·진한 배경)
 *  - lime    : Neon Lime (포인트 색, 버튼 배경·강조선·점) → 흰 배경 위 '글자색'으로는 쓰지 않음
 */
 const lime = {
  DEFAULT: '#B7F34D',
  dark: '#8FCB22',   // 아이콘·라인용
  soft: '#E5F5CF',
};

module.exports = {
  content: [
    './src/main/resources/templates/**/*.html',
    './src/main/resources/static/js/**/*.js',
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#263F56', // Navy
          dark: '#1D3246',    // hover
          deep: '#182A3B',    // 진한 배경(푸터·CTA)
          50: '#EEF2F5',
        },
        lime,
        surface: {
          DEFAULT: '#FFFFFF',
          low: '#F5F9F0',
          mid: '#E5F5CF',
        },
        line: {
          DEFAULT: '#E3E9DD',
          strong: '#C9D3C1',
        },
        ink: {
          DEFAULT: '#1B2B3A', // 제목
          soft: '#4A5866',    // 본문
          mute: '#7D8994',    // 보조
        },
      },
      fontFamily: {
        sans: ['"Pretendard Variable"', 'Pretendard', '-apple-system', 'BlinkMacSystemFont', 'system-ui', '"Apple SD Gothic Neo"', '"Malgun Gothic"', 'sans-serif'],
      },
      maxWidth: {
        site: '1200px',
      },
      boxShadow: {
        card: '0 1px 2px rgba(27,43,58,0.04)',
        lift: '0 6px 20px -8px rgba(27,43,58,0.16)',
      },
    },
  },
  plugins: [],
};