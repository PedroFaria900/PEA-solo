import { htmlReport } from './k6-reporter.bundle.js';
import { textSummary } from './k6-summary.js';

/**
 * Returns a handleSummary function that writes:
 *   - An HTML report to ${K6_REPORT_DIR}/<name>.html   (k6-reporter)
 *   - The default coloured text summary to stdout       (k6-summary)
 *
 * Usage (in each k6 script):
 *   import { makeHandleSummary } from './lib/summary.js';
 *   export const handleSummary = makeHandleSummary('stress');
 *
 * The output directory is read from the K6_REPORT_DIR env var, falling back
 * to 'k6_results'. The Makefile passes K6_REPORT_DIR=$(RESULTS_DIR) so the
 * HTML lands in the same timestamped dir as the CSV.
 *
 * @param {string} name - filename stem, e.g. 'stress', 'capacity', 'validacao-stress'
 */
export function makeHandleSummary(name) {
  return function handleSummary(data) {
    const dir  = __ENV.K6_REPORT_DIR || 'k6_results';
    const file = `${dir}/${name}.html`;
    return {
      [file]: htmlReport(data),
      stdout: textSummary(data, { indent: ' ', enableColors: true }),
    };
  };
}
