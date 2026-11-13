import { useEffect, useMemo, useRef } from "react";
import { ImagePlus, X } from "lucide-react";
import { toast } from "sonner";

export const MAX_IMAGE_BYTES = 5 * 1024 * 1024;
const ALLOWED = ["image/jpeg", "image/png", "image/webp"];

/** Vraća samo fajlove koji prolaze osnovnu proveru; za svaki odbijen prikazuje poruku (prava provera je na backend-u). */
export function filterImages(files: File[]): File[] {
  const ok: File[] = [];
  for (const f of files) {
    if (!ALLOWED.includes(f.type)) toast.error(`${f.name}: dozvoljeni su samo JPEG, PNG i WebP.`);
    else if (f.size > MAX_IMAGE_BYTES) toast.error(`${f.name}: najviše 5 MB po fotografiji.`);
    else ok.push(f);
  }
  return ok;
}

interface Props {
  files: File[];
  onChange: (files: File[]) => void;
  max: number;
}

export default function ImagePicker({ files, onChange, max }: Props) {
  const input = useRef<HTMLInputElement>(null);
  const urls = useMemo(() => files.map((f) => URL.createObjectURL(f)), [files]);
  useEffect(() => () => urls.forEach((u) => URL.revokeObjectURL(u)), [urls]);

  function add(list: FileList | null) {
    if (!list) return;
    const next = [...files, ...filterImages(Array.from(list))];
    if (next.length > max) toast.error(`Najviše ${max} fotografija.`);
    onChange(next.slice(0, max));
    if (input.current) input.current.value = "";
  }

  return (
    <div>
      <input ref={input} type="file" accept="image/jpeg,image/png,image/webp" multiple hidden onChange={(e) => add(e.target.files)} />
      <div className="grid grid-cols-3 gap-2">
        {urls.map((u, i) => (
          <div key={u} className="relative aspect-square overflow-hidden rounded-lg bg-neutral-100 dark:bg-neutral-800">
            <img src={u} alt="" className="h-full w-full object-cover" />
            <button type="button" aria-label="Ukloni" onClick={() => onChange(files.filter((_, j) => j !== i))}
              className="absolute right-1 top-1 rounded-full bg-black/60 p-0.5 text-white hover:bg-black">
              <X size={14} />
            </button>
          </div>
        ))}
        {files.length < max && (
          <button type="button" onClick={() => input.current?.click()}
            className="flex aspect-square flex-col items-center justify-center gap-1 rounded-lg border-2 border-dashed border-neutral-300 text-neutral-500 hover:border-neutral-400 dark:border-neutral-700">
            <ImagePlus size={24} />
            <span className="text-xs">Dodaj fotografije</span>
          </button>
        )}
      </div>
    </div>
  );
}
