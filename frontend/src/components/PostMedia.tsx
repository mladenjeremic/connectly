import { useState } from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { gradientFor } from "../lib/format";
import type { Post } from "../types";

interface Props {
  post: Post;
  onDoubleClick?: () => void;
}

/** Slike objave (sa strelicama i tačkicama) ili, ako nema slike, kartica sa tekstom na gradijentu. */
export default function PostMedia({ post, onDoubleClick }: Props) {
  const [index, setIndex] = useState(0);
  const images = post.images;

  if (images.length === 0) {
    return (
      <div onDoubleClick={onDoubleClick}
        className={`flex aspect-square w-full items-center justify-center bg-gradient-to-br ${gradientFor(post.id)} p-8 text-center`}>
        <p className="line-clamp-[10] break-words text-xl font-semibold text-white drop-shadow">{post.content}</p>
      </div>
    );
  }

  const safeIndex = Math.min(index, images.length - 1);
  return (
    <div className="relative select-none bg-black" onDoubleClick={onDoubleClick}>
      <img src={images[safeIndex].url} alt="" className="max-h-[75vh] w-full select-none object-contain" draggable={false} />
      {images.length > 1 && (
        <>
          {safeIndex > 0 && (
            <button aria-label="Prethodna" onClick={() => setIndex(safeIndex - 1)}
              className="absolute left-2 top-1/2 -translate-y-1/2 rounded-full bg-white/85 p-1.5 text-black shadow-md backdrop-blur hover:bg-white">
              <ChevronLeft size={18} />
            </button>
          )}
          {safeIndex < images.length - 1 && (
            <button aria-label="Sledeća" onClick={() => setIndex(safeIndex + 1)}
              className="absolute right-2 top-1/2 -translate-y-1/2 rounded-full bg-white/85 p-1.5 text-black shadow-md backdrop-blur hover:bg-white">
              <ChevronRight size={18} />
            </button>
          )}
          <div className="absolute inset-x-0 bottom-2 flex justify-center gap-1">
            {images.map((img, i) => (
              <span key={img.id} className={`h-1.5 rounded-full transition-all ${i === safeIndex ? "w-4 bg-white" : "w-1.5 bg-white/60"}`} />
            ))}
          </div>
        </>
      )}
    </div>
  );
}
