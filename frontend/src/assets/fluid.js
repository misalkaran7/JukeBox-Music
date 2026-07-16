const canvas = document.getElementById('fluid-canvas');
if (canvas) {
    const ctx = canvas.getContext('2d');
    let width, height;

    // Crimson / Burgundy color palette
    const colors = [
        'rgba(74, 14, 23, 0.4)',  // Deep Burgundy
        'rgba(139, 0, 0, 0.3)',   // Crimson Red
        'rgba(255, 0, 51, 0.15)', // Neon Red Accent
        'rgba(40, 5, 10, 0.6)'    // Ultra Dark Red
    ];

    let mouseX = 0;
    let mouseY = 0;
    let scrollY = 0;
    let targetScrollY = 0;

    window.addEventListener('resize', resizeCanvas);
    window.addEventListener('mousemove', (e) => {
        mouseX = (e.clientX - width / 2) * 0.5;
        mouseY = (e.clientY - height / 2) * 0.5;
    });
    window.addEventListener('scroll', () => {
        targetScrollY = window.scrollY * 0.5;
    });

    function resizeCanvas() {
        width = window.innerWidth;
        height = window.innerHeight;
        canvas.width = width;
        canvas.height = height;
    }

    class Blob {
        constructor(color, size, speed, angle, radiusX, radiusY) {
            this.color = color;
            this.size = size;
            this.speed = speed;
            this.angle = angle;
            this.baseRadiusX = radiusX;
            this.baseRadiusY = radiusY;
        }

        draw(time) {
            const x = width / 2 + Math.cos(this.angle + time * this.speed) * this.baseRadiusX + (mouseX * 0.3);
            const y = height / 2 + Math.sin(this.angle + time * this.speed) * this.baseRadiusY - scrollY;

            const gradient = ctx.createRadialGradient(x, y, 0, x, y, this.size);
            gradient.addColorStop(0, this.color);
            gradient.addColorStop(1, 'rgba(0, 0, 0, 0)');

            ctx.fillStyle = gradient;
            ctx.beginPath();
            ctx.arc(x, y, this.size, 0, Math.PI * 2);
            ctx.fill();
        }
    }

    const blobs = [];

    function initBlobs() {
        blobs.length = 0;
        const baseSize = Math.max(width, height);
        
        blobs.push(new Blob(colors[0], baseSize * 0.8, 0.0002, 0, baseSize * 0.2, baseSize * 0.3));
        blobs.push(new Blob(colors[1], baseSize * 0.9, 0.00015, Math.PI, baseSize * 0.3, baseSize * 0.2));
        blobs.push(new Blob(colors[2], baseSize * 0.6, 0.0003, Math.PI/2, baseSize * 0.15, baseSize * 0.4));
        blobs.push(new Blob(colors[3], baseSize * 1.2, 0.0001, Math.PI/4, baseSize * 0.4, baseSize * 0.1));
    }

    let startTime = Date.now();

    function animate() {
        const time = Date.now() - startTime;
        
        scrollY += (targetScrollY - scrollY) * 0.1;

        ctx.clearRect(0, 0, width, height);

        ctx.fillStyle = '#000000';
        ctx.fillRect(0, 0, width, height);

        ctx.globalCompositeOperation = 'screen';
        blobs.forEach(blob => blob.draw(time));
        ctx.globalCompositeOperation = 'source-over';

        requestAnimationFrame(animate);
    }

    resizeCanvas();
    initBlobs();
    animate();

    let resizeTimeout;
    window.addEventListener('resize', () => {
        clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(() => {
            initBlobs();
        }, 200);
    });
}
