const canvas = document.getElementById('fluid-canvas');
if (canvas) {
    const ctx = canvas.getContext('2d');
    let width, height;

    // Crimson / Burgundy color palette (perfect color)
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
        constructor(color, size, speed, angle, radiusX, radiusY, index) {
            this.color = color;
            this.size = size;
            this.speed = speed * 0.8; // subtle motion
            this.angle = angle;
            this.baseRadiusX = radiusX;
            this.baseRadiusY = radiusY;
            this.index = index;
        }

        draw(time) {
            const x = width / 2 + Math.cos(this.angle + time * this.speed) * this.baseRadiusX + (mouseX * 0.3);
            const y = height / 2 + Math.sin(this.angle + time * this.speed * 0.8) * this.baseRadiusY - scrollY;

            ctx.save();
            ctx.translate(x, y);
            // Rotate to create a sweeping wave effect
            ctx.rotate(this.angle + time * this.speed * 0.1);
            
            // Stretch out the blobs to make them look like fluid bands
            if (this.index % 2 === 0) {
                ctx.scale(2.5, 0.4);
            } else {
                ctx.scale(0.4, 2.5);
            }

            const gradient = ctx.createRadialGradient(0, 0, 0, 0, 0, this.size);
            gradient.addColorStop(0, this.color);
            gradient.addColorStop(1, 'rgba(0, 0, 0, 0)');

            ctx.fillStyle = gradient;
            ctx.beginPath();
            ctx.arc(0, 0, this.size, 0, Math.PI * 2);
            ctx.fill();
            
            ctx.restore();
        }
    }

    const blobs = [];

    function initBlobs() {
        blobs.length = 0;
        const baseSize = Math.max(width, height);
        
        // Add more blobs to create a complex marbling/wavy pattern
        for(let i=0; i<8; i++) {
            const color = colors[i % colors.length];
            const size = baseSize * (0.6 + Math.random() * 0.6);
            const speed = 0.0001 + Math.random() * 0.0001;
            const angle = Math.random() * Math.PI * 2;
            const rx = baseSize * (0.1 + Math.random() * 0.3);
            const ry = baseSize * (0.1 + Math.random() * 0.3);
            blobs.push(new Blob(color, size, speed, angle, rx, ry, i));
        }
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
