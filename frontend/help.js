// Inline help popover for all RPL pages.
// Usage: showHelp(event, 'pageName')  — called by the "?" button on each page.

const HELP = {
  dashboard: {
    title: "How the Dashboard works",
    sections: [
      {
        heading: "Pool Accounts",
        body: `Every resource type has a pool account that tracks available quantity. When an action is <strong>completed</strong>, the system withdraws the allocated quantity from the matching pool. Balances in <span style="color:#16a34a;font-weight:600">green</span> are healthy; <span style="color:#dc2626;font-weight:600">red</span> means over-consumed.`
      },
      {
        heading: "Over-consumption alerts",
        body: `If a pool balance drops below zero after a completion, the posting rule engine automatically creates an alert entry on the paired <em>Alert Memo</em> account. Go to <strong>Ledger</strong>, enter the memo account ID, and click <strong>Load</strong> to see the alert entries.`
      },
      {
        heading: "Quick Create",
        body: `Bootstrap data without hitting the API directly:<br>
• <strong>Protocol</strong> — a reusable template of steps.<br>
• <strong>Resource Type</strong> — also auto-creates a pool account and posting rule.<br>
• <strong>Plan</strong> — optionally link it to an existing protocol.`
      }
    ]
  },

  actions: {
    title: "How Actions work",
    sections: [
      {
        heading: "State machine",
        body: `<pre style="font-size:11px;background:#f5f5f5;color:#333;padding:10px;border-radius:4px;margin:4px 0;line-height:1.6">PROPOSED
  → submitForApproval → PENDING_APPROVAL
      → approve → IN_PROGRESS
      → reject  → PROPOSED
  → suspend → SUSPENDED → resume → IN_PROGRESS
  → abandon → ABANDONED
IN_PROGRESS → complete → COMPLETED
COMPLETED   → reopen  → REOPENED</pre>`
      },
      {
        heading: "Week 1 transitions",
        body: `<strong>Implement</strong> — moves directly to IN_PROGRESS.<br>
<strong>Suspend / Resume</strong> — pauses and resumes; records a suspension entry.<br>
<strong>Complete</strong> — finalises the action and triggers ledger entries.<br>
<strong>Abandon</strong> — terminates permanently.`
      },
      {
        heading: "Week 2 transitions",
        body: `<strong>Submit for Approval</strong> — PROPOSED → PENDING_APPROVAL.<br>
<strong>Approve</strong> — PENDING_APPROVAL → IN_PROGRESS, creates ImplementedAction.<br>
<strong>Reject</strong> — returns to PROPOSED for revision.<br>
<strong>Reopen</strong> — COMPLETED → REOPENED, posts reversal entries to restore pool.`
      },
      {
        heading: "Testing over-consumption",
        body: `1. Add a consumable allocation with a large quantity.<br>
2. Submit for Approval → Approve (or just Implement).<br>
3. Complete — the pool is withdrawn.<br>
4. Check <strong>Dashboard</strong> for a red balance, then <strong>Ledger</strong> for alert entries.`
      }
    ]
  },

  ledger: {
    title: "How the Ledger works",
    sections: [
      {
        heading: "Double-entry entries",
        body: `Every completion creates a <strong>LedgerTransaction</strong> with two entries:<br>
• A <strong>withdrawal</strong> (negative) from the resource pool.<br>
• A <strong>deposit</strong> (positive) on a usage tracking account.<br>
Asset allocations use <em>time period hours</em> as the amount instead of quantity.`
      },
      {
        heading: "Alert memo accounts",
        body: `When a pool goes negative, the posting rule posts an extra entry on the <strong>Alert Memo</strong> account. Use the shortcut buttons to quickly jump to any memo account.`
      },
      {
        heading: "Filtering",
        body: `<strong>Show All</strong> — every entry for this account.<br>
<strong>Consumable</strong> — GENERAL/POOL allocations.<br>
<strong>Asset</strong> — SPECIFIC asset allocations (identified by account name).`
      },
      {
        heading: "Reversal entries",
        body: `When an action is <strong>reopened</strong>, reversal entries restore the pool balance. They appear as <span style="color:#16a34a;font-weight:600">positive</span> amounts on the pool account.`
      }
    ]
  },

  plans: {
    title: "How Plans work",
    sections: [
      {
        heading: "Plan tree",
        body: `A Plan is a composite tree of <strong>ProposedActions</strong> (leaves) and nested <strong>sub-Plans</strong>. A plan's status rolls up from its leaves — it is COMPLETED only when all leaf actions are completed.`
      },
      {
        heading: "Depth limit slider",
        body: `Limits how many levels are expanded. Sub-plans beyond the limit show as <em>(collapsed)</em>. This mirrors the <strong>LazySubtreeIterator</strong> in the backend.`
      },
      {
        heading: "Plan Metrics",
        body: `<strong>Completion ratio</strong> — % of leaf actions completed.<br>
<strong>Total resource cost</strong> — sum of quantity × unit cost.<br>
<strong>Risk score</strong> — count of SUSPENDED or ABANDONED leaves.<br>
Computed by the Visitor pattern (CompletionRatioVisitor, ResourceCostVisitor, RiskScoreVisitor).`
      }
    ]
  },

  report: {
    title: "How the Report works",
    sections: [
      {
        heading: "Depth-first traversal",
        body: `The report walks the plan tree using a <strong>DepthFirstPlanIterator</strong>, visiting every node in depth-first order. Each line is indented by depth level.`
      },
      {
        heading: "Status filter (API)",
        body: `Append <code>?status=IN_PROGRESS</code> to the API call to activate a <strong>FilteredPlanIterator</strong> that skips non-matching nodes. The UI shows the full unfiltered report by default.`
      }
    ]
  }
};

// ── Popover ──────────────────────────────────────────────────────────────────

// Lazily create popover on first use — avoids DOMContentLoaded timing issues
function getOrCreatePopover() {
  let pop = document.getElementById("helpPopover");
  if (pop) return pop;

  pop = document.createElement("div");
  pop.className = "help-popover";
  pop.id = "helpPopover";
  pop.innerHTML = `
    <div class="help-pop-header">
      <span class="help-pop-title" id="helpPopTitle"></span>
      <button class="help-pop-close" onclick="closeHelp()">✕</button>
    </div>
    <div id="helpPopBody"></div>`;
  document.body.appendChild(pop);

  // Click outside to close
  document.addEventListener("click", e => {
    const p = document.getElementById("helpPopover");
    if (p && p.classList.contains("open")
        && !p.contains(e.target)
        && !e.target.closest(".help-btn")) {
      closeHelp();
    }
  });

  return pop;
}

(function injectStyles() {
  const s = document.createElement("style");
  s.textContent = `
    .help-popover {
      position: fixed;
      z-index: 500;
      background: #fff;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      box-shadow: 0 4px 20px rgba(0,0,0,0.12);
      width: 320px;
      max-height: 70vh;
      overflow-y: auto;
      padding: 18px 20px 20px;
      display: none;
    }
    .help-popover.open { display: block; }
    .help-pop-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 14px;
    }
    .help-pop-title {
      font-size: 13px;
      font-weight: 700;
      color: #111;
    }
    .help-pop-close {
      background: none;
      border: none;
      font-size: 16px;
      cursor: pointer;
      color: #888;
      line-height: 1;
      padding: 0 2px;
    }
    .help-pop-close:hover { color: #111; }
    .help-pop-section { margin-bottom: 14px; }
    .help-pop-section:last-child { margin-bottom: 0; }
    .help-pop-section h3 {
      font-size: 11px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.06em;
      color: #2563eb;
      margin-bottom: 5px;
    }
    .help-pop-section p {
      font-size: 12.5px;
      line-height: 1.65;
      color: #333;
    }
    .help-btn {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: #2563eb;
      background: #eff6ff;
      border: 1px solid #bfdbfe;
      border-radius: 4px;
      padding: 3px 10px;
      cursor: pointer;
      font-weight: 500;
      transition: background 0.1s;
    }
    .help-btn:hover { background: #dbeafe; }
    .help-btn.active { background: #dbeafe; border-color: #93c5fd; }
  `;
  document.head.appendChild(s);
})();

function showHelp(event, btnOrPage, page) {
  // Support both signatures:
  //   showHelp(event, this, 'page')  — preferred (passes button element directly)
  //   showHelp(event, 'page')        — legacy (resolves button via event.target)
  let btn, pageKey;
  if (typeof btnOrPage === "string") {
    pageKey = btnOrPage;
    btn = (event.target && event.target.closest)
            ? (event.target.closest(".help-btn") || event.target)
            : event.target;
  } else {
    pageKey = page;
    btn = btnOrPage;
  }

  const data = HELP[pageKey];
  if (!data) return;

  const pop = getOrCreatePopover();

  // Toggle off if already open for same page
  if (pop.classList.contains("open") && pop.dataset.page === pageKey) {
    closeHelp();
    return;
  }

  pop.dataset.page = pageKey;
  document.getElementById("helpPopTitle").textContent = data.title;
  document.getElementById("helpPopBody").innerHTML = data.sections.map(s => `
    <div class="help-pop-section">
      <h3>${s.heading}</h3>
      <p>${s.body}</p>
    </div>`).join("");

  // Position: prefer right side of main panel; fall back to below button
  const btnRect = btn.getBoundingClientRect();
  const popWidth = 300;
  const mainEl = document.querySelector(".main");
  let left, top;

  if (mainEl) {
    const mainRect = mainEl.getBoundingClientRect();
    const spaceRight = window.innerWidth - mainRect.right;
    if (spaceRight >= popWidth + 24) {
      // Enough blank space to the right
      left = mainRect.right + 12;
      top = btnRect.top;
    } else {
      // Not enough space — drop below button, right-aligned to button
      left = btnRect.right - popWidth;
      top = btnRect.bottom + 8;
    }
  } else {
    left = btnRect.right - popWidth;
    top = btnRect.bottom + 8;
  }

  // Clamp horizontally and vertically within viewport
  left = Math.max(8, Math.min(left, window.innerWidth - popWidth - 8));
  top  = Math.max(8, Math.min(top,  window.innerHeight - 100));

  pop.style.width = popWidth + "px";
  pop.style.left  = left + "px";
  pop.style.top   = top  + "px";
  pop.classList.add("open");

  btn.classList.add("active");
  pop._btn = btn;
}

function closeHelp() {
  const pop = document.getElementById("helpPopover"); // don't force-create on close
  if (!pop) return;
  pop.classList.remove("open");
  if (pop._btn) { pop._btn.classList.remove("active"); pop._btn = null; }
}
