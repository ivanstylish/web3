(function () {
    'use strict';

    const validX = [-2, -1.5, -1, -0.5, 0, 0.5, 1, 1.5, 2];

    window.addEventListener('load', init);

    function init() {
        const canvas = document.getElementById('areaCanvas');
        if (!canvas) {
            console.error("Canvas not found!");
            return;
        }

        const ctx = canvas.getContext('2d');
        const W = canvas.width;
        const H = canvas.height;
        const cx = W / 2;
        const cy = H / 2;
        const PADDING = 30;
        const ppu = (W / 2 - PADDING) / 5;

        function getR() {
            const hidden = document.getElementById('pointForm:rValue');
            const slider = document.getElementById('rSlider');
            if (hidden && hidden.value) return parseFloat(hidden.value);
            if (slider && slider.value) return parseFloat(slider.value);
            return 2.0;
        }

        function draw() {
            const r = getR();

            ctx.clearRect(0, 0, W, H);
            ctx.fillStyle = '#fff';
            ctx.fillRect(0, 0, W, H);

            ctx.fillStyle = 'rgba(33, 150, 243, 0.5)';
            ctx.strokeStyle = '#1E88E5';

            // 正方形
            ctx.beginPath();
            ctx.rect(cx - r*ppu, cy - r*ppu, r*ppu, r*ppu);
            ctx.fill();
            ctx.stroke();

            // 三角形
            ctx.beginPath();
            ctx.moveTo(cx, cy);
            ctx.lineTo(cx - r*ppu, cy);
            ctx.lineTo(cx, cy + (r/2)*ppu);
            ctx.closePath();
            ctx.fill();
            ctx.stroke();

            // 扇形
            ctx.beginPath();
            ctx.moveTo(cx, cy);
            ctx.arc(cx, cy, (r / 2) * ppu, 0, Math.PI / 2, false); // 逆时针，从0到90度（数学第四象限）
            ctx.closePath();
            ctx.fill();
            ctx.stroke();

            drawAxes(r);

            drawHistoryPoints();
        }

        function drawAxes(r) {
            ctx.strokeStyle = '#000';
            ctx.lineWidth = 1;
            ctx.fillStyle = '#000';
            ctx.font = '12px Arial';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';

            ctx.beginPath();
            ctx.moveTo(PADDING, cy); ctx.lineTo(W - PADDING, cy);
            ctx.moveTo(cx, PADDING); ctx.lineTo(cx, H - PADDING);
            ctx.stroke();

            const markLabels = [
                {val: r, label: 'R'},
                {val: r / 2, label: 'R/2'},
            ];

            ctx.beginPath();
            ctx.moveTo(W - PADDING, cy); ctx.lineTo(W - PADDING - 10, cy - 5);
            ctx.lineTo(W - PADDING, cy); ctx.lineTo(W - PADDING - 10, cy + 5);
            ctx.moveTo(cx, PADDING); ctx.lineTo(cx - 5, PADDING + 10);
            ctx.lineTo(cx, PADDING); ctx.lineTo(cx + 5, PADDING + 10);
            ctx.stroke();

            markLabels.forEach(mark => {
                const pix = mark.val * ppu;
                const label = mark.label;

                if(r === 0 || isNaN(pix) || mark.val <= 0) return;

                ctx.beginPath();
                ctx.moveTo(cx + pix, cy - 3); ctx.lineTo(cx + pix, cy + 3);
                ctx.stroke();
                ctx.fillText(label, cx + pix, cy + 15);

                ctx.beginPath();
                ctx.moveTo(cx - pix, cy - 3); ctx.lineTo(cx - pix, cy + 3);
                ctx.stroke();
                ctx.fillText(`-${label}`, cx - pix, cy + 15);

                ctx.beginPath();
                ctx.moveTo(cx - 3, cy - pix); ctx.lineTo(cx + 3, cy - pix);
                ctx.stroke();
                ctx.fillText(label, cx - 15, cy - pix);

                ctx.beginPath();
                ctx.moveTo(cx - 3, cy + pix); ctx.lineTo(cx + 3, cy + pix);
                ctx.stroke();
                ctx.fillText(`-${label}`, cx - 15, cy + pix);
            });

            ctx.font = 'bold 14px Arial';
            ctx.fillText('X', W-10, cy-10);
            ctx.fillText('Y', cx+10, 10);
        }

        function drawHistoryPoints() {
            const rows = document.querySelectorAll('#historyTable tbody tr');
            rows.forEach(row => {
                const cells = row.children;
                if(cells.length >= 5) {
                    const x = parseFloat(cells[1].innerText);
                    const y = parseFloat(cells[2].innerText);
                    const pointR = parseFloat(cells[3].innerText); // 使用点自己的R值
                    const hitText = cells[4].querySelector('span') ? cells[4].querySelector('span').innerText : cells[4].innerText;
                    const hit = hitText.includes('YES');

                    if(!isNaN(x) && !isNaN(y) && !isNaN(pointR)) {
                        const currentR = getR();
                        const scale = currentR / pointR; // 缩放比例

                        const displayX = x * scale;
                        const displayY = y * scale;

                        ctx.beginPath();
                        ctx.arc(cx + displayX*ppu, cy - displayY*ppu, 4, 0, Math.PI*2);

                        ctx.fillStyle = hit ? '#4CAF50' : '#F44336';
                        ctx.fill();
                        ctx.strokeStyle = '#333';
                        ctx.stroke();
                    }
                }
            });
        }

        window.redrawGraph = draw;

        canvas.addEventListener('click', function(e) {
            const rect = canvas.getBoundingClientRect();
            const xRaw = (e.clientX - rect.left - cx) / ppu;
            const yRaw = (cy - (e.clientY - rect.top)) / ppu;

            const xVal = validX.reduce((prev, curr) =>
                Math.abs(curr - xRaw) < Math.abs(prev - xRaw) ? curr : prev
            );
            const yVal = parseFloat(yRaw.toFixed(2));

            document.getElementById('pointForm:hiddenX').value = xVal;
            document.getElementById('pointForm:hiddenY').value = yVal;

            const yInput = document.getElementById('pointForm:yInput');
            if(yInput) yInput.value = yVal;


            const canvasBtn = document.getElementById('pointForm:canvasBtn');
            if (canvasBtn) {
                canvasBtn.click();
            } else {
                console.error("Canvas button not found!");
            }

            const existingError = document.getElementById('canvasClickError');
            if (existingError) {
                existingError.remove();
            }

            if (yVal < -3 || yVal > 3) {
                const errorMsg = document.createElement('span');
                errorMsg.id = 'canvasClickError';
                errorMsg.style.color = 'red';
                errorMsg.style.font = 'Arial'
                errorMsg.style.fontSize = '12px';
                errorMsg.style.marginLeft = '10px';
                errorMsg.textContent = 'Y coordinate out of range [-3, 3]';

                if (yInput) {
                    const formRow = yInput.closest('.form-row');
                    if (formRow) {
                        formRow.appendChild(errorMsg);
                    }
                }

                setTimeout(() => {
                    if (errorMsg.parentNode) {
                        errorMsg.remove();
                    }
                }, 3000);

                return;
            }

            document.getElementById('pointForm:hiddenX').value = xVal;
            document.getElementById('pointForm:hiddenY').value = yVal;

            if(window.prepareSubmit) window.prepareSubmit(true);
            document.querySelector('.btn-primary').click();
        });

        draw();
    }
})();

function updateR(val) {
    document.getElementById('rDisplay').textContent = parseFloat(val).toFixed(1);
    document.getElementById('pointForm:rValue').value = val;
    if(window.redrawGraph) window.redrawGraph();
}

function prepareSubmit(isCanvasClick) {
    // 同步R
    const slider = document.getElementById('rSlider');
    if(slider) document.getElementById('pointForm:rValue').value = slider.value;

    if (!isCanvasClick) {
        const hx = document.getElementById('pointForm:hiddenX');
        const hy = document.getElementById('pointForm:hiddenY');
        if(hx) hx.value = "";
        if(hy) hy.value = "";
    }
    return true;
}

function handleAjaxEvent(data) {
    if(data.status === "success") {
        const hiddenR = document.getElementById('pointForm:rValue');
        const slider = document.getElementById('rSlider');
        if(hiddenR && slider) {
            slider.value = hiddenR.value;
            document.getElementById('rDisplay').textContent = parseFloat(hiddenR.value).toFixed(1);
        }
        if(window.redrawGraph) window.redrawGraph();
    }
}

function handleClearEvent(data) {
    if(data.status === "success" && window.redrawGraph) window.redrawGraph();
}

function showModal() {
    document.getElementById('customModal').style.display = 'block';
}
function closeModal() {
    document.getElementById('customModal').style.display = 'none';
}
function confirmClear() {
    closeModal();
    const btn = document.getElementById('pointForm:realClearBtn') || document.querySelector('[id$="realClearBtn"]');
    if(btn) btn.click();
}

window.onclick = function(event) {
    const modal = document.getElementById('customModal');
    if (event.target == modal) {
        modal.style.display = "none";
    }
}