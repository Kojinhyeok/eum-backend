/** 이음인사이트 브랜드 팔레트: A. Neon Mint & Deep Teal
 *  #FFFFFF / #F1F8FA / #D8EDF0 / #2EE6BE / #315E74
 */
module.exports = {
  content: [
    './src/main/resources/templates/**/*.html',
    './src/main/resources/static/js/**/*.js',
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#315E74', // Deep Teal (메인)
          dark: '#264B5D',    // hover
          deep: '#1B3644',    // 진한 배경(푸터·CTA)
          50: '#EAF3F6',
        },
        mint: {
          DEFAULT: '#2EE6BE', // Neon Mint (포인트)
          dark: '#14B48F',    // 아이콘·라인용 (본문 글자색으로는 사용 X)
          soft: '#D6FAF1',
        },
        surface: {
          DEFAULT: '#FFFFFF',
          low: '#F1F8FA',
          mid: '#D8EDF0',
        },
        line: {
          DEFAULT: '#D8EDF0',
          strong: '#BCD7DE',
        },
        ink: {
          DEFAULT: '#14303D', // 제목
          soft: '#47616D',    // 본문
          mute: '#7A919B',    // 보조
        },
      },
      fontFamily: {
        sans: ['"Pretendard Variable"', 'Pretendard', '-apple-system', 'BlinkMacSystemFont', 'system-ui', '"Apple SD Gothic Neo"', '"Malgun Gothic"', 'sans-serif'],
      },
      maxWidth: {
        site: '1200px',
      },
      boxShadow: {
        card: '0 1px 2px rgba(20,48,61,0.04), 0 4px 16px -4px rgba(20,48,61,0.06)',
        lift: '0 10px 30px -10px rgba(20,48,61,0.18)',
      },
    },
  },
  plugins: [],
};
